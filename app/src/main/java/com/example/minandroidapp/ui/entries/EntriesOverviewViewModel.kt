package com.example.minandroidapp.ui.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.model.EntrySharePayload
import com.example.minandroidapp.model.LogEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntriesOverviewViewModel(private val repository: QuickLogRepository) : ViewModel() {

    private val entriesFlow = repository.observeEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val tagQuery = MutableStateFlow("")
    private val _selectedEntryIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedEntryIds: StateFlow<Set<Long>> = _selectedEntryIds.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
        .withZone(ZoneId.systemDefault())

    private val shareFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())

    val entriesByDate: StateFlow<List<EntryListItem>> = entriesFlow
        .map { entries ->
            buildSections(entries.groupBy { dateFormatter.format(it.createdAt) })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val entriesByLocation: StateFlow<List<EntryListItem>> = entriesFlow
        .map { entries ->
            val groups = entries.groupBy { it.location.label ?: "Unknown" }
            buildSections(groups)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val tagGroups: StateFlow<List<TagGroupItem>> = entriesFlow
        .map { entries ->
            entries
                .flatMap { entry -> entry.tags.map { tag -> tag to entry } }
                .groupBy({ it.first.id to it.first.label }, { it.second })
                .map { (key, groupedEntries) ->
                    TagGroupItem(tagId = key.first, tagLabel = key.second, entries = groupedEntries)
                }
                .sortedByDescending { it.entries.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredTagGroups: StateFlow<List<TagGroupItem>> = combine(tagGroups, tagQuery) { groups, query ->
        if (query.isBlank()) groups else groups.filter { it.tagLabel.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val entryStats: StateFlow<EntryStats> = entriesFlow
        .map { entries ->
            val total = entries.size
            val tagCounts = entries.flatMap { it.tags }.groupingBy { it.id }.eachCount()
            val top = tagCounts.maxByOrNull { it.value }
            val uniqueTags = tagCounts.keys.size
            val firstEntry = entries.minByOrNull(LogEntry::createdAt)?.createdAt
            val lastEntry = entries.maxByOrNull(LogEntry::createdAt)?.createdAt
            val avg = if (total == 0 || firstEntry == null || lastEntry == null) {
                0.0
            } else {
                val days = ((lastEntry.epochSecond - firstEntry.epochSecond) / 86400.0).coerceAtLeast(1.0)
                total / days
            }
            EntryStats(
                total = total,
                uniqueTags = uniqueTags,
                topTagLabel = top?.key?.let { id ->
                    entries.firstNotNullOfOrNull { entry -> entry.tags.firstOrNull { it.id == id }?.label }
                },
                topTagCount = top?.value ?: 0,
                averagePerDay = avg,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryStats(0, 0, null, 0, 0.0))

    fun setTagQuery(query: String) {
        tagQuery.value = query
    }

    fun buildEntryListFromTags(tagId: String): List<EntryListItem> {
        val entries = entriesFlow.value.filter { entry -> entry.tags.any { it.id == tagId } }
        return buildSections(mapOf("Results" to entries))
    }

    fun formatSharePreview(entry: LogEntry): String {
        val timestamp = dateFormatter.format(entry.createdAt)
        val tags = entry.tags.joinToString(separator = ", ") { it.label }
        return "$timestamp • $tags"
    }

    val dateCounts: StateFlow<List<StatCount>> = entriesFlow
        .map { entries -> buildCounts(entries) { dateFormatter.format(it.createdAt) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val locationCounts: StateFlow<List<StatCount>> = entriesFlow
        .map { entries -> buildCounts(entries) { it.location.label ?: "Unknown" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tagCounts: StateFlow<List<StatCount>> = entriesFlow
        .map { entries ->
            entries
                .flatMap { entry -> entry.tags.map { tag -> tag.label } }
                .groupingBy { it }
                .eachCount()
                .map { StatCount(it.key, it.value) }
                .sortedByDescending { it.count }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            entriesFlow.collect { entries ->
                val existingIds = entries.map { it.id }.toSet()
                val filteredSelection = _selectedEntryIds.value.filter { existingIds.contains(it) }.toSet()
                if (filteredSelection.size != _selectedEntryIds.value.size) {
                    _selectedEntryIds.value = filteredSelection
                }
            }
        }
    }

    fun buildSharePayload(entry: LogEntry): EntrySharePayload {
        val timestamp = shareFormatter.format(entry.createdAt)
        val tagsBlock = entry.tags.joinToString(separator = ", ") { it.label }
        val location = entry.location.label?.let { " • location: $it" } ?: ""
        val note = entry.note?.takeIf { it.isNotBlank() }?.let { " • note: $it" } ?: ""
        val plain = "- $timestamp • tags: $tagsBlock$location$note"
        val html = buildString {
            append("<p><strong>").append(timestamp).append("</strong><br/>")
            append("<strong>Tags:</strong> ").append(tagsBlock)
            entry.location.label?.let { append("<br/><strong>Location:</strong> ").append(it) }
            entry.note?.takeIf { it.isNotBlank() }?.let {
                append("<br/><strong>Note:</strong> ").append(it.replace("<", "&lt;").replace(">", "&gt;"))
            }
            append("</p>")
        }
        return EntrySharePayload(plain = plain, html = html)
    }

    private fun buildSections(groups: Map<String, List<LogEntry>>): List<EntryListItem> {
        val items = mutableListOf<EntryListItem>()
        groups.toSortedMap().forEach { (title, list) ->
            if (list.isNotEmpty()) {
                items += EntryListItem.Header(title)
                items += list.sortedByDescending { it.createdAt }.map { EntryListItem.Entry(it) }
            }
        }
        return items
    }

    private fun buildCounts(entries: List<LogEntry>, selector: (LogEntry) -> String): List<StatCount> {
        return entries
            .groupingBy(selector)
            .eachCount()
            .map { StatCount(it.key, it.value) }
            .sortedByDescending { it.count }
    }

    fun toggleEntrySelection(entryId: Long) {
        val updated = _selectedEntryIds.value.toMutableSet().apply {
            if (!add(entryId)) {
                remove(entryId)
            }
        }
        _selectedEntryIds.value = updated
    }

    fun clearEntrySelection() {
        if (_selectedEntryIds.value.isNotEmpty()) {
            _selectedEntryIds.value = emptySet()
        }
    }

    fun deleteEntries(entryIds: List<Long>, onComplete: (Boolean) -> Unit) {
        if (entryIds.isEmpty()) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            runCatching { repository.deleteEntries(entryIds) }
                .onSuccess {
                    _selectedEntryIds.value = _selectedEntryIds.value - entryIds.toSet()
                    onComplete(true)
                }
                .onFailure { onComplete(false) }
        }
    }

    fun deleteSelectedEntries(onComplete: (Boolean) -> Unit) {
        deleteEntries(_selectedEntryIds.value.toList(), onComplete)
    }

    class Factory(private val repository: QuickLogRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(EntriesOverviewViewModel::class.java)) {
                "Unknown ViewModel class"
            }
            return EntriesOverviewViewModel(repository) as T
        }
    }
}
