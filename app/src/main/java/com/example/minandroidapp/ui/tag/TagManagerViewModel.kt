package com.example.minandroidapp.ui.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.model.TagRelations
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagManagerViewModel(private val repository: QuickLogRepository) : ViewModel() {

    val tagRelations: StateFlow<List<TagRelations>> = repository.observeTagRelations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateConnections(tagId: String, relatedIds: Set<String>) {
        viewModelScope.launch {
            repository.updateTagRelations(tagId, relatedIds)
        }
    }

    suspend fun exportTagsCsv(): String = repository.exportTagsCsv()

    suspend fun importTagsCsv(csv: String): Int = repository.importTagsCsv(csv)

    class Factory(private val repository: QuickLogRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TagManagerViewModel::class.java)) {
                "Unknown ViewModel class"
            }
            return TagManagerViewModel(repository) as T
        }
    }
}
