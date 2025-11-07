package com.example.minandroidapp.data

import androidx.room.withTransaction
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.data.db.entities.EntryEntity
import com.example.minandroidapp.data.db.entities.EntryTagCrossRef
import com.example.minandroidapp.data.db.entities.TagEntity
import com.example.minandroidapp.data.db.entities.TagLinkEntity
import com.example.minandroidapp.data.mappers.toModel
import com.example.minandroidapp.model.EntryLocation
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.model.LogTag
import com.example.minandroidapp.model.TagCategory
import com.example.minandroidapp.model.TagRelations
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class QuickLogRepository(private val database: LogDatabase) {

    private val tagDao = database.tagDao()
    private val entryDao = database.entryDao()

    fun observeRecentTags(limit: Int): Flow<List<LogTag>> {
        return tagDao.observeRecentTags(limit).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun observeEntries(): Flow<List<LogEntry>> {
        return entryDao.observeEntries().map { entries ->
            entries.map { it.toModel() }
        }
    }

    suspend fun getSuggestions(selectedTagIds: Set<String>): List<LogTag> {
        if (selectedTagIds.isEmpty()) {
            return emptyList()
        }
        return tagDao.getSuggestions(selectedTagIds.toList()).map { it.toModel() }
    }

    suspend fun getTags(tagIds: Collection<String>): List<LogTag> {
        if (tagIds.isEmpty()) {
            return emptyList()
        }
        return tagDao.getTags(tagIds.toList()).map { it.toModel() }
    }

    suspend fun getEntry(entryId: Long): LogEntry? {
        return entryDao.getEntry(entryId)?.toModel()
    }

    suspend fun saveEntry(
        entryId: Long?,
        createdAt: Instant,
        note: String?,
        location: EntryLocation,
        tagIds: Set<String>,
    ) {
        if (tagIds.isEmpty()) {
            throw IllegalArgumentException("An entry requires at least one tag")
        }

        val sanitizedNote = note?.trim()?.takeIf { it.isNotEmpty() }

        database.withTransaction {
            val entry = EntryEntity(
                id = entryId ?: 0,
                createdAt = createdAt,
                note = sanitizedNote,
                latitude = location.latitude,
                longitude = location.longitude,
                locationLabel = location.label,
            )

            val newId = if (entryId == null) {
                entryDao.insertEntry(entry)
            } else {
                entryDao.insertEntry(entry)
                entryId
            }

            entryDao.deleteEntryTags(newId)
            val relations = tagIds.map { tagId ->
                EntryTagCrossRef(entryId = newId, tagId = tagId)
            }
            entryDao.insertEntryTags(relations)

            val now = Instant.now()
            tagDao.touchTags(tagIds.toList(), now)
        }
    }

    suspend fun deleteEntry(entryId: Long) {
        database.withTransaction {
            entryDao.deleteEntryTags(entryId)
            entryDao.deleteEntry(entryId)
        }
    }

    suspend fun createCustomTag(label: String): LogTag {
        val trimmed = label.trim()
        require(trimmed.isNotEmpty()) { "Tag label cannot be empty" }
        val tag = TagEntity(
            id = "user_${UUID.randomUUID()}",
            label = trimmed,
            category = TagCategory.CUSTOM,
            lastUsedAt = Instant.now(),
        )
        tagDao.insertTag(tag)
        return tag.toModel()
    }

    fun observeTagRelations(): Flow<List<TagRelations>> {
        return combine(tagDao.observeAllTags(), tagDao.observeLinks()) { tags, links ->
            val tagMap = tags.associateBy { it.id }
            val adjacency = links.groupBy({ it.parentTagId }, { it.childTagId })
            tags.map { tag ->
                val related = adjacency[tag.id]
                    ?.mapNotNull { childId -> tagMap[childId]?.toModel() }
                    ?.sortedBy { it.label }
                    ?: emptyList()
                TagRelations(tag = tag.toModel(), related = related)
            }
        }
    }

    suspend fun updateTagRelations(tagId: String, desiredIds: Set<String>) {
        val current = tagDao.getAllLinks()
            .filter { it.parentTagId == tagId }
            .map { it.childTagId }
            .toSet()
        val toAdd = desiredIds - current
        val toRemove = current - desiredIds
        database.withTransaction {
            toAdd.forEach { childId ->
                tagDao.insertLink(TagLinkEntity(parentTagId = tagId, childTagId = childId))
                tagDao.insertLink(TagLinkEntity(parentTagId = childId, childTagId = tagId))
            }
            toRemove.forEach { childId ->
                tagDao.deleteLink(tagId, childId)
                tagDao.deleteLink(childId, tagId)
            }
        }
    }

    fun searchEntriesByTags(selectedTagIds: Set<String>): Flow<List<LogEntry>> {
        if (selectedTagIds.isEmpty()) {
            return observeEntries()
        }
        return observeEntries().map { entries ->
            entries.filter { entry ->
                entry.tags.any { selectedTagIds.contains(it.id) }
            }
        }
    }

    fun exportEntriesAsCsv(entries: List<LogEntry>): String {
        val header = "timestamp,location,tags,note"
        val rows = entries.joinToString(separator = "\n") { entry ->
            val timestamp = entry.createdAt.toString()
            val location = entry.location.label?.replace(",", " ") ?: ""
            val tags = entry.tags.joinToString(separator = "|") { it.label.replace(",", " ") }
            val note = entry.note?.replace(",", " ")?.replace("\n", " ") ?: ""
            listOf(timestamp, location, tags, note)
                .joinToString(separator = ",") { value ->
                    if (value.contains("\"") || value.contains(",")) {
                        "\"" + value.replace("\"", "\"\"") + "\""
                    } else {
                        value
                    }
                }
        }
        return header + "\n" + rows
    }

    suspend fun exportTagsCsv(): String {
        val tags = tagDao.getAllTags()
        val links = tagDao.getAllLinks()
        val related = links.groupBy { it.parentTagId }
            .mapValues { entry -> entry.value.map { it.childTagId }.toSet() }
        return buildString {
            append("id,label,category,related_ids\n")
            tags.forEach { tag ->
                val relatedIds = related[tag.id]?.joinToString(separator = "|") ?: ""
                append(tag.id.escapeCsv()).append(',')
                    .append(tag.label.escapeCsv()).append(',')
                    .append(tag.category.name.escapeCsv()).append(',')
                    .append(relatedIds.escapeCsv())
                    .append('\n')
            }
        }
    }

    suspend fun importTagsCsv(csv: String): Int {
        val lines = csv.lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .toList()
        if (lines.isEmpty()) return 0

        val tags = mutableListOf<TagEntity>()
        val links = mutableListOf<Pair<String, String>>()
        lines.forEach { line ->
            val columns = parseCsvLine(line)
            if (columns.size < 3) return@forEach
            val id = columns[0].ifBlank { "import_${UUID.randomUUID()}" }
            val label = columns[1].ifBlank { id }
            val category = columns.getOrNull(2)?.let { runCatching { TagCategory.valueOf(it) }.getOrNull() }
                ?: TagCategory.CUSTOM
            tags += TagEntity(id = id, label = label, category = category)
            val relatedColumn = columns.getOrNull(3).orEmpty()
            relatedColumn.split('|')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { relatedId -> links += id to relatedId }
        }

        database.withTransaction {
            tagDao.insertTags(tags)
            links.forEach { (parent, child) ->
                tagDao.insertLink(TagLinkEntity(parentTagId = parent, childTagId = child))
                tagDao.insertLink(TagLinkEntity(parentTagId = child, childTagId = parent))
            }
        }
        return tags.size
    }

    private fun String.escapeCsv(): String =
        if (contains(',') || contains('"')) "\"${replace("\"", "\"\"")}\"" else this

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            i++
        }
        result += current.toString()
        return result
    }
}
