package com.example.minandroidapp.data.db.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.minandroidapp.data.db.entities.EntryEntity
import com.example.minandroidapp.data.db.entities.EntryTagCrossRef
import com.example.minandroidapp.data.db.entities.TagEntity

data class EntryWithTags(
    @Embedded val entry: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity>,
)
