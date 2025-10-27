package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgb.mrfcmanager.data.repository.ProponentRepository

/**
 * Factory for creating ProponentViewModel with dependencies
 */
class ProponentViewModelFactory(
    private val repository: ProponentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProponentViewModel::class.java)) {
            return ProponentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
