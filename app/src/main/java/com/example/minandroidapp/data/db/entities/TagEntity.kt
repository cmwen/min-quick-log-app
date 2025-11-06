package com.example.minandroidapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.minandroidapp.model.TagCategory
import java.time.Instant

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val label: String,
    val category: TagCategory,
    val lastUsedAt: Instant? = null,
)
