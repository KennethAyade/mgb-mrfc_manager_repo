package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgb.mrfcmanager.data.repository.MrfcRepository

/**
 * Factory for creating MrfcViewModel with dependencies
 */
class MrfcViewModelFactory(
    private val repository: MrfcRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MrfcViewModel::class.java)) {
            return MrfcViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
