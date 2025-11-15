package com.example.minandroidapp.ui.map

import java.time.Instant

data class LocationEntry(
    val id: Long,
    val createdAt: Instant,
    val latitude: Double?,
    val longitude: Double?,
    val locationLabel: String?,
    val tags: List<String>
)
