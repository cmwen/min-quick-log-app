package com.example.minandroidapp.data.mappers

import com.example.minandroidapp.data.db.entities.EntryEntity
import com.example.minandroidapp.data.db.entities.TagEntity
import com.example.minandroidapp.data.db.models.EntryWithTags
import com.example.minandroidapp.model.EntryLocation
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.model.LogTag

fun TagEntity.toModel(): LogTag = LogTag(
    id = id,
    label = label,
    category = category,
    lastUsedAt = lastUsedAt,
)

fun EntryWithTags.toModel(): LogEntry = LogEntry(
    id = entry.id,
    createdAt = entry.createdAt,
    note = entry.note,
    location = EntryLocation(
        latitude = entry.latitude,
        longitude = entry.longitude,
        label = entry.locationLabel,
    ),
    tags = tags.sortedBy { it.label }.map(TagEntity::toModel),
)

fun EntryEntity.toModel(tags: List<TagEntity>): LogEntry = LogEntry(
    id = id,
    createdAt = createdAt,
    note = note,
    location = EntryLocation(
        latitude = latitude,
        longitude = longitude,
        label = locationLabel,
    ),
    tags = tags.sortedBy { it.label }.map(TagEntity::toModel),
)
