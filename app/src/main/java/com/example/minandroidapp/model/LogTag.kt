package com.example.minandroidapp.model

import java.time.Instant

data class LogTag(
    val id: String,
    val label: String,
    val category: TagCategory,
    val lastUsedAt: Instant?,
)
