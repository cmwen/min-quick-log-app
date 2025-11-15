package com.example.minandroidapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.model.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

                _locationEntries.value = filtered.map { entry: LogEntry ->
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
