package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.ActionItem
import com.mgb.mrfcmanager.data.remote.dto.CreateMinutesRequest
import com.mgb.mrfcmanager.data.remote.dto.Decision
import com.mgb.mrfcmanager.data.remote.dto.MeetingMinutesDto
import com.mgb.mrfcmanager.data.repository.MinutesRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Meeting Minutes management
 * Handles minutes state and operations with approval workflow
 */
class MinutesViewModel(private val repository: MinutesRepository) : ViewModel() {

    private val _minutesState = MutableLiveData<MinutesState>()
    val minutesState: LiveData<MinutesState> = _minutesState

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    /**
     * Load minutes for a specific agenda
     */
    fun loadMinutesByAgenda(agendaId: Long) {
        _minutesState.value = MinutesState.Loading

        viewModelScope.launch {
            when (val result = repository.getMinutesByAgenda(agendaId)) {
                is Result.Success -> {
                    _minutesState.value = MinutesState.Success(result.data)
                }
                is Result.Error -> {
                    _minutesState.value = MinutesState.Error(result.message)
                }
                is Result.Loading -> {
                    _minutesState.value = MinutesState.Loading
                }
            }
        }
    }

    /**
     * Create or update meeting minutes
     */
    fun saveMinutes(
        agendaId: Long,
        summary: String,
        decisions: List<Decision>,
        actionItems: List<ActionItem>,
        isFinal: Boolean = false,
        onComplete: (Result<MeetingMinutesDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateMinutesRequest(
                agendaId = agendaId,
                summary = summary,
                decisions = decisions,
                actionItems = actionItems,
                isFinal = isFinal
            )

            val result = repository.createMinutes(request)
            onComplete(result)

            // Reload on success
            if (result is Result.Success) {
                _minutesState.value = MinutesState.Success(result.data)
                _isEditMode.value = false
            }
        }
    }

    /**
     * Update existing minutes
     */
    fun updateMinutes(
        id: Long,
        agendaId: Long,
        summary: String,
        decisions: List<Decision>,
        actionItems: List<ActionItem>,
        isFinal: Boolean = false,
        onComplete: (Result<MeetingMinutesDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateMinutesRequest(
                agendaId = agendaId,
                summary = summary,
                decisions = decisions,
                actionItems = actionItems,
                isFinal = isFinal
            )

            val result = repository.updateMinutes(id, request)
            onComplete(result)

            // Reload on success
            if (result is Result.Success) {
                _minutesState.value = MinutesState.Success(result.data)
                _isEditMode.value = false
            }
        }
    }

    /**
     * Approve minutes (mark as final)
     */
    fun approveMinutes(id: Long, onComplete: (Result<MeetingMinutesDto>) -> Unit) {
        viewModelScope.launch {
            val result = repository.approveMinutes(id)
            onComplete(result)

            // Reload on success
            if (result is Result.Success) {
                _minutesState.value = MinutesState.Success(result.data)
            }
        }
    }

    /**
     * Delete meeting minutes
     */
    fun deleteMinutes(id: Long, agendaId: Long, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteMinutes(id)
            onComplete(result)

            // Reload on success
            if (result is Result.Success) {
                loadMinutesByAgenda(agendaId)
            }
        }
    }

    /**
     * Toggle edit mode
     */
    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
    }

    /**
     * Refresh minutes
     */
    fun refresh(agendaId: Long) {
        loadMinutesByAgenda(agendaId)
    }

    /**
     * Clear state
     */
    fun clearState() {
        _minutesState.value = MinutesState.Idle
        _isEditMode.value = false
    }
}

/**
 * Sealed class representing minutes state
 */
sealed class MinutesState {
    object Idle : MinutesState()
    object Loading : MinutesState()
    data class Success(val data: MeetingMinutesDto) : MinutesState()
    data class Error(val message: String) : MinutesState()
}
