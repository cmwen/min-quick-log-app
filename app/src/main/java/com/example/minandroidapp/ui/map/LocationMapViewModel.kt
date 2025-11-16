package com.example.minandroidapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.model.EntryLocation
import com.example.minandroidapp.model.LogTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

class LocationMapViewModel(private val repository: QuickLogRepository) : ViewModel() {

    private val _locationEntries = MutableStateFlow<List<LocationEntry>>(emptyList())
    val locationEntries: StateFlow<List<LocationEntry>> = _locationEntries.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilter(null, null))
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            repository.observeAllEntries().collect { entries ->
                val filter = _dateFilter.value
                val filtered = if (filter.startDate != null && filter.endDate != null) {
                    entries.filter { entry ->
                        entry.createdAt >= filter.startDate && entry.createdAt <= filter.endDate
                    }
                } else {
                    entries
                }

                _locationEntries.value = filtered
                    .filter { entry -> entry.location.latitude != null && entry.location.longitude != null }
                    .map { entry: LogEntry ->
                        LocationEntry(
                            id = entry.id,
                            createdAt = entry.createdAt,
                            latitude = entry.location.latitude,
                            longitude = entry.location.longitude,
                            locationLabel = entry.location.label,
                            tags = entry.tags.map { tag -> tag.label }
                        )
                    }
            }
        }
    }

    fun setDateFilter(startDate: Instant, endDate: Instant) {
        _dateFilter.value = DateFilter(startDate, endDate)
        loadEntries()
    }

    fun clearDateFilter() {
        _dateFilter.value = DateFilter(null, null)
        loadEntries()
    }

    fun exportEntriesAsJson(): String {
        val entries = _locationEntries.value
        if (entries.isEmpty()) return ""

        val jsonBuilder = StringBuilder()
        jsonBuilder.append("{\n")
        jsonBuilder.append("  \"entries\": [\n")
        
        entries.forEachIndexed { index, entry ->
            jsonBuilder.append("    {\n")
            jsonBuilder.append("      \"id\": ${entry.id},\n")
            jsonBuilder.append("      \"timestamp\": \"${entry.createdAt}\",\n")
            jsonBuilder.append("      \"latitude\": ${entry.latitude},\n")
            jsonBuilder.append("      \"longitude\": ${entry.longitude},\n")
            jsonBuilder.append("      \"location\": \"${entry.locationLabel?.replace("\"", "\\\"") ?: ""}\",\n")
            jsonBuilder.append("      \"tags\": [${entry.tags.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }}]\n")
            jsonBuilder.append("    }")
            if (index < entries.size - 1) jsonBuilder.append(",")
            jsonBuilder.append("\n")
        }
        
        jsonBuilder.append("  ],\n")
        jsonBuilder.append("  \"metadata\": {\n")
        jsonBuilder.append("    \"total_entries\": ${entries.size},\n")
        jsonBuilder.append("    \"exported_at\": \"${Instant.now()}\"\n")
        jsonBuilder.append("  }\n")
        jsonBuilder.append("}")
        
        return jsonBuilder.toString()
    }

    fun exportEntriesAsCsv(): String {
        val entries = _locationEntries.value
        if (entries.isEmpty()) return ""

        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Timestamp,Latitude,Longitude,Location,Tags\n")
        
        entries.forEach { entry ->
            csvBuilder.append("${entry.id},")
            csvBuilder.append("\"${entry.createdAt}\",")
            csvBuilder.append("${entry.latitude ?: ""},")
            csvBuilder.append("${entry.longitude ?: ""},")
            csvBuilder.append("\"${entry.locationLabel?.replace("\"", "\"\"") ?: ""}\",")
            csvBuilder.append("\"${entry.tags.joinToString("; ").replace("\"", "\"\"")}\"\n")
        }
        
        return csvBuilder.toString()
    }

    suspend fun importLocationsCsv(csv: String): Int {
        val lines = csv.lineSequence()
            .filter { it.isNotBlank() }
            .dropWhile { !it.contains("ID,Timestamp", ignoreCase = true) }
            .drop(1)
            .toList()
        if (lines.isEmpty()) return 0

        // Map existing tags by label for quick lookup
        val existingTags: MutableMap<String, LogTag> = repository.observeAllTags().first()
            .associateBy { it.label.lowercase() }
            .toMutableMap()

        var imported = 0
        lines.forEach { line ->
            val cols = parseCsv(line)
            if (cols.size < 6) return@forEach
            val timestamp = cols[1].trim('"')
            val lat = cols[2].toDoubleOrNull()
            val lon = cols[3].toDoubleOrNull()
            val label = cols[4].trim('"')
            val tagsStr = cols[5].trim('"')
            if (lat == null || lon == null) return@forEach
            val createdAt = runCatching { Instant.parse(timestamp) }.getOrElse {
                // Allow non-ISO if exported by this app (Instant.toString is ISO), else skip
                return@forEach
            }
            val tagLabels = tagsStr.split(';').map { it.trim() }.filter { it.isNotEmpty() }
            val tagIds = mutableSetOf<String>()
            for (lbl in tagLabels) {
                val key = lbl.lowercase()
                val tag = existingTags[key] ?: run {
                    val newTag = repository.createCustomTag(lbl)
                    existingTags[key] = newTag
                    newTag
                }
                tagIds += tag.id
            }
            if (tagIds.isEmpty()) {
                val defKey = "location"
                val def = existingTags[defKey] ?: repository.createCustomTag("Location").also {
                    existingTags[defKey] = it
                }
                tagIds += def.id
            }
            repository.saveEntry(
                entryId = null,
                createdAt = createdAt,
                note = null,
                location = EntryLocation(latitude = lat, longitude = lon, label = label.ifBlank { null }),
                tagIds = tagIds,
            )
            imported++
        }
        // Reload
        loadEntries()
        return imported
    }

    private fun parseCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result += current.toString()
        return result
    }

    class Factory(private val repository: QuickLogRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationMapViewModel::class.java)) {
                return LocationMapViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
