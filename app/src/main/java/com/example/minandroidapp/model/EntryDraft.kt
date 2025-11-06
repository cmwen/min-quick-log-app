package com.example.minandroidapp.model

import java.time.Instant

data class EntryDraft(
    val entryId: Long? = null,
    val createdAt: Instant = Instant.now(),
    val selectedTags: List<LogTag> = emptyList(),
    val note: String = "",
    val location: EntryLocation = EntryLocation(null, null, null),
)
