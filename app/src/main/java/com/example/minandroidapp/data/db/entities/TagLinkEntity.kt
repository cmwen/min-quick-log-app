package com.example.minandroidapp.data.db.entities

import androidx.room.Entity

@Entity(
    tableName = "tag_links",
    primaryKeys = ["parentTagId", "childTagId"],
)
data class TagLinkEntity(
    val parentTagId: String,
    val childTagId: String,
)
