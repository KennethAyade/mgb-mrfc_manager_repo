package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.CreateMatterArisingRequest
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingDto
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingSummary
import com.mgb.mrfcmanager.data.repository.MatterArisingRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Matters Arising management
 * Handles matters arising state and operations with status filtering
 */
class MatterArisingViewModel(private val repository: MatterArisingRepository) : ViewModel() {

    private val _mattersListState = MutableLiveData<MattersListState>()
    val mattersListState: LiveData<MattersListState> = _mattersListState

    private val _mattersSummary = MutableLiveData<MatterArisingSummary?>()
    val mattersSummary: LiveData<MatterArisingSummary?> = _mattersSummary

    private val _filterStatus = MutableLiveData<String?>(null)
    val filterStatus: LiveData<String?> = _filterStatus

    /**
     * Load matters arising for a specific meeting
     */
    fun loadMattersByAgenda(agendaId: Long) {
        _mattersListState.value = MattersListState.Loading

        viewModelScope.launch {
            when (val result = repository.getMattersByAgenda(agendaId)) {
                is Result.Success -> {
                    val filteredMatters = applyStatusFilter(result.data.matters)
                    _mattersListState.value = MattersListState.Success(filteredMatters)
                    _mattersSummary.value = result.data.summary
                }
                is Result.Error -> {
                    _mattersListState.value = MattersListState.Error(result.message)
                    _mattersSummary.value = null
                }
                is Result.Loading -> {
                    _mattersListState.value = MattersListState.Loading
                }
            }
        }
    }

    /**
     * Create new matter arising
     */
    fun createMatter(
        agendaId: Long,
        issue: String,
        status: String = "PENDING",
        assignedTo: String? = null,
        onComplete: (Result<MatterArisingDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateMatterArisingRequest(
                agendaId = agendaId,
                issue = issue,
                assignedTo = assignedTo,
                dateRaised = getCurrentDate(),
                status = status
            )
            val result = repository.createMatter(request)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadMattersByAgenda(agendaId)
            }
        }
    }

    /**
     * Update matter arising (primarily for status changes)
     */
    fun updateMatter(
        id: Long,
        agendaId: Long,
        issue: String,
        status: String,
        assignedTo: String? = null,
        onComplete: (Result<MatterArisingDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateMatterArisingRequest(
                agendaId = agendaId,
                issue = issue,
                assignedTo = assignedTo,
                dateRaised = getCurrentDate(), // Backend will keep original date
                status = status
            )
            val result = repository.updateMatter(id, request)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadMattersByAgenda(agendaId)
            }
        }
    }

    /**
     * Update matter status (convenience method)
     * Note: Requires full matter details - should load matter first, then update
     */
    fun updateMatterStatus(
        id: Long,
        agendaId: Long,
        issue: String,
        newStatus: String,
        assignedTo: String? = null,
        onComplete: (Result<MatterArisingDto>) -> Unit
    ) {
        updateMatter(id, agendaId, issue, newStatus, assignedTo, onComplete)
    }

    /**
     * Delete matter arising
     */
    fun deleteMatter(id: Long, agendaId: Long, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteMatter(id)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadMattersByAgenda(agendaId)
            }
        }
    }

    /**
     * Set status filter (PENDING, IN_PROGRESS, RESOLVED, or null for all)
     */
    fun setStatusFilter(status: String?) {
        _filterStatus.value = status
        // Re-apply filter to current data
        val currentState = _mattersListState.value
        if (currentState is MattersListState.Success) {
            // Note: This assumes we cached the full list somewhere
            // For now, just trigger a reload
        }
    }

    /**
     * Apply status filter to matters list
     */
    private fun applyStatusFilter(matters: List<MatterArisingDto>): List<MatterArisingDto> {
        val filter = _filterStatus.value
        return if (filter != null) {
            matters.filter { it.status == filter }
        } else {
            matters
        }
    }

    /**
     * Refresh matters list
     */
    fun refresh(agendaId: Long) {
        loadMattersByAgenda(agendaId)
    }

    /**
     * Clear filter
     */
    fun clearFilter() {
        _filterStatus.value = null
    }

    /**
     * Get current date (helper for date_raised)
     */
    fun getCurrentDate(): String {
        val currentDate = java.util.Calendar.getInstance()
        val year = currentDate.get(java.util.Calendar.YEAR)
        val month = currentDate.get(java.util.Calendar.MONTH) + 1
        val day = currentDate.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
}

/**
 * Sealed class representing matters arising list state
 */
sealed class MattersListState {
    object Idle : MattersListState()
    object Loading : MattersListState()
    data class Success(val data: List<MatterArisingDto>) : MattersListState()
    data class Error(val message: String) : MattersListState()
}
