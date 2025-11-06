package com.example.minandroidapp.model

sealed class QuickLogEvent {
    data class EntrySaved(val updated: Boolean) : QuickLogEvent()
    data class Error(val message: String) : QuickLogEvent()
}
