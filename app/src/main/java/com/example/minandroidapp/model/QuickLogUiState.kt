package com.example.minandroidapp.model

import java.time.Instant

data class QuickLogUiState(
    val draft: EntryDraft = EntryDraft(),
    val recentTags: List<LogTag> = emptyList(),
    val connectedTags: List<LogTag> = emptyList(),
    val suggestedTags: List<LogTag> = emptyList(),
    val entries: List<LogEntry> = emptyList(),
    val clockInstant: Instant = Instant.now(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
