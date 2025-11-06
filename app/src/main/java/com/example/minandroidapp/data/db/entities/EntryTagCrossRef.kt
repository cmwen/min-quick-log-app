package com.example.minandroidapp.data.db.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "entry_tags",
    primaryKeys = ["entryId", "tagId"],
    indices = [
        Index(value = ["tagId"]),
        Index(value = ["entryId"]),
    ],
)
data class EntryTagCrossRef(
    val entryId: Long,
    val tagId: String,
)
