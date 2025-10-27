package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgb.mrfcmanager.data.repository.AttendanceRepository

/**
 * Factory for creating AttendanceViewModel instances
 */
class AttendanceViewModelFactory(
    private val repository: AttendanceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            return AttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
