package com.example.minandroidapp.ui.entries

import com.example.minandroidapp.model.LogEntry

sealed class EntryListItem {
    data class Header(val title: String) : EntryListItem()
    data class Entry(val logEntry: LogEntry) : EntryListItem()
}
