package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.ProponentDto
import com.mgb.mrfcmanager.data.repository.ProponentRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Proponent list and detail screens
 * Handles proponent state and operations
 */
class ProponentViewModel(private val repository: ProponentRepository) : ViewModel() {

    private val _proponentListState = MutableLiveData<ProponentListState>()
    val proponentListState: LiveData<ProponentListState> = _proponentListState

    private val _proponentDetailState = MutableLiveData<ProponentDetailState>()
    val proponentDetailState: LiveData<ProponentDetailState> = _proponentDetailState

    /**
     * Load all proponents for a specific MRFC
     */
    fun loadProponentsByMrfc(mrfcId: Long, status: String? = null) {
        _proponentListState.value = ProponentListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllProponents(mrfcId = mrfcId, status = status)) {
                is Result.Success -> {
                    _proponentListState.value = ProponentListState.Success(result.data)
                }
                is Result.Error -> {
                    _proponentListState.value = ProponentListState.Error(result.message)
                }
                is Result.Loading -> {
                    _proponentListState.value = ProponentListState.Loading
                }
            }
        }
    }

    /**
     * Load all proponents (no MRFC filter)
     */
    fun loadAllProponents(status: String? = null) {
        _proponentListState.value = ProponentListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllProponents(status = status)) {
                is Result.Success -> {
                    _proponentListState.value = ProponentListState.Success(result.data)
                }
                is Result.Error -> {
                    _proponentListState.value = ProponentListState.Error(result.message)
                }
                is Result.Loading -> {
                    _proponentListState.value = ProponentListState.Loading
                }
            }
        }
    }

    /**
     * Load proponent by ID
     */
    fun loadProponentById(id: Long) {
        _proponentDetailState.value = ProponentDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getProponentById(id)) {
                is Result.Success -> {
                    _proponentDetailState.value = ProponentDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _proponentDetailState.value = ProponentDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _proponentDetailState.value = ProponentDetailState.Loading
                }
            }
        }
    }

    /**
     * Refresh proponent list
     */
    fun refresh(mrfcId: Long? = null, status: String? = null) {
        if (mrfcId != null) {
            loadProponentsByMrfc(mrfcId, status)
        } else {
            loadAllProponents(status)
        }
    }
}

/**
 * Sealed class representing proponent list state
 */
sealed class ProponentListState {
    object Idle : ProponentListState()
    object Loading : ProponentListState()
    data class Success(val data: List<ProponentDto>) : ProponentListState()
    data class Error(val message: String) : ProponentListState()
}

/**
 * Sealed class representing proponent detail state
 */
sealed class ProponentDetailState {
    object Idle : ProponentDetailState()
    object Loading : ProponentDetailState()
    data class Success(val data: ProponentDto) : ProponentDetailState()
    data class Error(val message: String) : ProponentDetailState()
}
