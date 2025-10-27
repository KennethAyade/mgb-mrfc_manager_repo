package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaRequest
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Agenda management and viewing
 * Handles agenda state and operations
 */
class AgendaViewModel(private val repository: AgendaRepository) : ViewModel() {

    private val _agendaListState = MutableLiveData<AgendaListState>()
    val agendaListState: LiveData<AgendaListState> = _agendaListState

    private val _agendaDetailState = MutableLiveData<AgendaDetailState>()
    val agendaDetailState: LiveData<AgendaDetailState> = _agendaDetailState

    /**
     * Load agendas by MRFC and quarter
     */
    fun loadAgendasByMrfcAndQuarter(mrfcId: Long, quarter: String, year: Int? = null) {
        _agendaListState.value = AgendaListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllAgendas(
                mrfcId = mrfcId,
                quarter = quarter,
                year = year
            )) {
                is Result.Success -> {
                    _agendaListState.value = AgendaListState.Success(result.data)
                }
                is Result.Error -> {
                    _agendaListState.value = AgendaListState.Error(result.message)
                }
                is Result.Loading -> {
                    _agendaListState.value = AgendaListState.Loading
                }
            }
        }
    }

    /**
     * Load all agendas for a specific MRFC
     */
    fun loadAgendasByMrfc(mrfcId: Long) {
        _agendaListState.value = AgendaListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllAgendas(mrfcId = mrfcId)) {
                is Result.Success -> {
                    _agendaListState.value = AgendaListState.Success(result.data)
                }
                is Result.Error -> {
                    _agendaListState.value = AgendaListState.Error(result.message)
                }
                is Result.Loading -> {
                    _agendaListState.value = AgendaListState.Loading
                }
            }
        }
    }

    /**
     * Load all agendas (no filter)
     */
    fun loadAllAgendas() {
        _agendaListState.value = AgendaListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllAgendas()) {
                is Result.Success -> {
                    _agendaListState.value = AgendaListState.Success(result.data)
                }
                is Result.Error -> {
                    _agendaListState.value = AgendaListState.Error(result.message)
                }
                is Result.Loading -> {
                    _agendaListState.value = AgendaListState.Loading
                }
            }
        }
    }

    /**
     * Load agenda by ID (with items)
     */
    fun loadAgendaById(id: Long) {
        _agendaDetailState.value = AgendaDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getAgendaById(id)) {
                is Result.Success -> {
                    _agendaDetailState.value = AgendaDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _agendaDetailState.value = AgendaDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _agendaDetailState.value = AgendaDetailState.Loading
                }
            }
        }
    }

    /**
     * Create a new agenda
     */
    suspend fun createAgenda(request: CreateAgendaRequest): Result<AgendaDto> {
        return repository.createAgenda(request)
    }

    /**
     * Update an existing agenda
     */
    suspend fun updateAgenda(id: Long, request: CreateAgendaRequest): Result<AgendaDto> {
        return repository.updateAgenda(id, request)
    }

    /**
     * Delete an agenda
     */
    suspend fun deleteAgenda(id: Long): Result<Unit> {
        return repository.deleteAgenda(id)
    }

    /**
     * Refresh agenda list
     */
    fun refresh(mrfcId: Long? = null, quarter: String? = null, year: Int? = null) {
        if (mrfcId != null && quarter != null) {
            loadAgendasByMrfcAndQuarter(mrfcId, quarter, year)
        } else if (mrfcId != null) {
            loadAgendasByMrfc(mrfcId)
        } else {
            loadAllAgendas()
        }
    }
}

/**
 * Sealed class representing agenda list state
 */
sealed class AgendaListState {
    object Idle : AgendaListState()
    object Loading : AgendaListState()
    data class Success(val data: List<AgendaDto>) : AgendaListState()
    data class Error(val message: String) : AgendaListState()
}

/**
 * Sealed class representing agenda detail state
 */
sealed class AgendaDetailState {
    object Idle : AgendaDetailState()
    object Loading : AgendaDetailState()
    data class Success(val data: AgendaDto) : AgendaDetailState()
    data class Error(val message: String) : AgendaDetailState()
}
