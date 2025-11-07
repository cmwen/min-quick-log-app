package com.example.minandroidapp.ui.entries

data class EntryStats(
    val total: Int,
    val uniqueTags: Int,
    val topTagLabel: String?,
    val topTagCount: Int,
    val averagePerDay: Double,
)
