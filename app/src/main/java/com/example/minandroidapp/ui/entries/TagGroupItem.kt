package com.example.minandroidapp.ui.entries

import com.example.minandroidapp.model.LogEntry

data class TagGroupItem(
    val tagLabel: String,
    val tagId: String,
    val entries: List<LogEntry>,
)
