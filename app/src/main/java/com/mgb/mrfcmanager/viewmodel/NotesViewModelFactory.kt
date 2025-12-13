package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgb.mrfcmanager.data.repository.OfflineNotesRepository

/**
 * Factory for creating NotesViewModel instances
 * Supports offline-first repository with user context
 */
class NotesViewModelFactory(
    private val repository: OfflineNotesRepository,
    private val userId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
