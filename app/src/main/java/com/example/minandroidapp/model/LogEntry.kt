package com.example.minandroidapp.model

import java.time.Instant

data class LogEntry(
    val id: Long,
    val createdAt: Instant,
    val note: String?,
    val location: EntryLocation,
    val tags: List<LogTag>,
)
