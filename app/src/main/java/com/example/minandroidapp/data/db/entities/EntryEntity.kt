package com.example.minandroidapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Instant,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationLabel: String?,
)
