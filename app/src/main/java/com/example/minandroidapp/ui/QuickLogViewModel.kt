package com.example.minandroidapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.model.EntryDraft
import com.example.minandroidapp.model.EntryLocation
import com.example.minandroidapp.model.EntrySharePayload
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.model.LogTag
import com.example.minandroidapp.model.QuickLogEvent
import com.example.minandroidapp.model.QuickLogUiState
import com.example.minandroidapp.model.TagRelations
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class QuickLogViewModel(private val repository: QuickLogRepository) : ViewModel() {

    private val recentTagsFlow = repository.observeRecentTags(limit = 12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val entriesFlow = repository.observeEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allTagsFlow = repository.observeAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val draftFlow = MutableStateFlow(EntryDraft())
    private val selectedTagIdsFlow = draftFlow.mapSelectedTagIds()
    private val clockFlow = MutableStateFlow(Instant.now())
    private val isSavingFlow = MutableStateFlow(false)
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val currentLocationFlow = MutableStateFlow(EntryLocation(null, null, null))
    private val eventsFlow = MutableSharedFlow<QuickLogEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var tickerJob: Job? = null

    private val tagRelationsFlow = repository.observeTagRelations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val topConnectedTagsFlow: StateFlow<List<LogTag>> = combine(
        tagRelationsFlow,
        selectedTagIdsFlow,
    ) { relations, selectedIds ->
        relations
            .filter { it.related.isNotEmpty() }
            .sortedWith(
                compareByDescending<TagRelations> { it.related.size }
                    .thenBy { it.tag.label.lowercase(Locale.getDefault()) },
            )
            .map { it.tag }
            .filter { it.id !in selectedIds }
            .take(POPULAR_TAG_LIMIT)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val suggestionsFlow: StateFlow<List<LogTag>> = selectedTagIdsFlow
        .flatMapLatest { selectedIds ->
            flow {
                val candidates = repository.getSuggestions(selectedIds)
                val filtered = candidates.filter { candidate ->
                    selectedIds.contains(candidate.id).not()
                }
                emit(filtered)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val draftWithContextFlow = combine(
        draftFlow,
        recentTagsFlow,
        topConnectedTagsFlow,
        suggestionsFlow,
    ) { draft, recentTags, connectedTags, suggestions ->
        DraftContext(
            draft = draft,
            recentTags = recentTags,
            connectedTags = connectedTags,
            suggestions = suggestions,
        )
    }

    private val exportFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    val uiState: StateFlow<QuickLogUiState> = combine(
        draftWithContextFlow,
        entriesFlow,
        clockFlow,
        isSavingFlow,
        errorMessageFlow,
    ) { draftContext, entries, clock, isSaving, error ->
        QuickLogUiState(
            draft = draftContext.draft,
            recentTags = draftContext.recentTags,
            connectedTags = draftContext.connectedTags,
            suggestedTags = draftContext.suggestions,
            entries = entries,
            clockInstant = clock,
            isSaving = isSaving,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, QuickLogUiState())

    val events = eventsFlow.asSharedFlow()

    val allTags: StateFlow<List<LogTag>> = allTagsFlow

    init {
        startClockTicker()
    }

    fun toggleTag(tag: LogTag) {
        errorMessageFlow.value = null
        draftFlow.update { draft ->
            val current = draft.selectedTags.toMutableList()
            val index = current.indexOfFirst { it.id == tag.id }
            if (index >= 0) {
                current.removeAt(index)
            } else {
                current.add(tag)
            }
            draft.copy(selectedTags = current)
        }
    }

    fun clearTags() {
        draftFlow.update { draft ->
            draft.copy(selectedTags = emptyList())
        }
    }

    fun setNote(note: String) {
        draftFlow.update { draft ->
            draft.copy(note = note)
        }
    }

    fun startNewEntry() {
        val location = currentLocationFlow.value
        draftFlow.value = EntryDraft(
            createdAt = Instant.now(),
            location = location,
        )
        errorMessageFlow.value = null
    }

    fun beginEditing(entryId: Long) {
        viewModelScope.launch {
            val entry = repository.getEntry(entryId) ?: return@launch
            draftFlow.value = EntryDraft(
                entryId = entry.id,
                createdAt = entry.createdAt,
                selectedTags = entry.tags,
                note = entry.note.orEmpty(),
                location = entry.location,
            )
            errorMessageFlow.value = null
        }
    }

    fun updateLocation(location: EntryLocation) {
        currentLocationFlow.value = location
        draftFlow.update { draft ->
            if (draft.entryId == null) {
                draft.copy(location = location)
            } else {
                draft
            }
        }
    }

    fun applyCurrentLocationToDraft() {
        val location = currentLocationFlow.value
        draftFlow.update { draft ->
            draft.copy(location = location)
        }
    }

    fun saveEntry() {
        val snapshot = draftFlow.value
        if (snapshot.selectedTags.isEmpty()) {
            errorMessageFlow.value = "Select at least one tag before saving."
            return
        }

        viewModelScope.launch {
            isSavingFlow.value = true
            try {
                repository.saveEntry(
                    entryId = snapshot.entryId,
                    createdAt = snapshot.createdAt,
                    note = snapshot.note,
                    location = snapshot.location,
                    tagIds = snapshot.selectedTags.map { it.id }.toSet(),
                )
                eventsFlow.emit(QuickLogEvent.EntrySaved(updated = snapshot.entryId != null))
                errorMessageFlow.value = null
                draftFlow.value = EntryDraft(
                    createdAt = Instant.now(),
                    location = currentLocationFlow.value,
                )
            } catch (error: Exception) {
                errorMessageFlow.value = error.message ?: "Unable to save entry"
                eventsFlow.emit(
                    QuickLogEvent.Error(error.message ?: "Unable to save entry"),
                )
            } finally {
                isSavingFlow.value = false
            }
        }
    }

    fun exportLogAsText(): String {
        val entries = uiState.value.entries
        if (entries.isEmpty()) {
            return ""
        }
        return entries.joinToString(separator = "\n") { formatEntryText(it) }
    }

    fun exportLogAsHtml(): String {
        val entries = uiState.value.entries
        if (entries.isEmpty()) {
            return ""
        }
        return entries.joinToString(separator = "") { formatEntryHtml(it) }
    }

    fun exportEntry(entry: LogEntry): EntrySharePayload {
        return EntrySharePayload(
            plain = formatEntryText(entry),
            html = formatEntryHtml(entry),
        )
    }

    fun exportEntriesCsv(): String {
        return repository.exportEntriesAsCsv(uiState.value.entries)
    }

    fun createCustomTag(label: String) {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            viewModelScope.launch {
                eventsFlow.emit(QuickLogEvent.Error("Tag name cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val tag = repository.createCustomTag(trimmed)
                toggleTag(tag)
                eventsFlow.emit(QuickLogEvent.CustomTagCreated(tag.label))
            } catch (error: Exception) {
                eventsFlow.emit(QuickLogEvent.Error(error.message ?: "Unable to create tag"))
            }
        }
    }

    fun selectTagByLabel(label: String) {
        val tag = allTagsFlow.value.firstOrNull { it.label.equals(label, ignoreCase = true) }
            ?: return
        if (draftFlow.value.selectedTags.any { it.id == tag.id }) return
        toggleTag(tag)
    }

    private fun startClockTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                clockFlow.value = Instant.now()
                delay(30_000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }

    class Factory(private val repository: QuickLogRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(QuickLogViewModel::class.java)) {
                "Unknown ViewModel class"
            }
            return QuickLogViewModel(repository) as T
        }
    }

    companion object {
        private const val POPULAR_TAG_LIMIT = 8
    }

    private fun formatEntryText(entry: LogEntry): String {
        val timestamp = exportFormatter.format(entry.createdAt)
        val tagsBlock = entry.tags.joinToString(separator = ", ") { it.label }
        val location = entry.location.label?.let { " • location: $it" } ?: ""
        val note = entry.note?.takeIf { it.isNotBlank() }?.let { " • note: $it" } ?: ""
        return "- $timestamp • tags: $tagsBlock$location$note"
    }

    private fun formatEntryHtml(entry: LogEntry): String {
        val timestamp = exportFormatter.format(entry.createdAt)
        val tagsBlock = entry.tags.joinToString(separator = ", ") { it.label }
        val location = entry.location.label?.let { "<br/><strong>Location:</strong> $it" } ?: ""
        val note = entry.note?.takeIf { it.isNotBlank() }?.let { "<br/><strong>Note:</strong> ${escapeHtml(it)}" } ?: ""
        return """<p><strong>$timestamp</strong><br/><strong>Tags:</strong> $tagsBlock$location$note</p>"""
    }

    private fun escapeHtml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

private data class DraftContext(
    val draft: EntryDraft,
    val recentTags: List<LogTag>,
    val connectedTags: List<LogTag>,
    val suggestions: List<LogTag>,
)

@OptIn(ExperimentalCoroutinesApi::class)
private fun MutableStateFlow<EntryDraft>.mapSelectedTagIds(): Flow<Set<String>> = this
    .distinctUntilChanged { old, new ->
        old.selectedTags.map { it.id } == new.selectedTags.map { it.id }
    }
    .flatMapLatest { draft ->
        flow {
            emit(draft.selectedTags.map { it.id }.toSet())
        }
    }
