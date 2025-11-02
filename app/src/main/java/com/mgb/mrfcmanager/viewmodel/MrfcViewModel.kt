package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.CreateMrfcRequest
import com.mgb.mrfcmanager.data.remote.dto.UpdateMrfcRequest
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for MRFC list screen
 * Handles MRFC list state and operations
 */
class MrfcViewModel(private val repository: MrfcRepository) : ViewModel() {

    private val _mrfcListState = MutableLiveData<MrfcListState>()
    val mrfcListState: LiveData<MrfcListState> = _mrfcListState

    private val _mrfcDetailState = MutableLiveData<MrfcDetailState>()
    val mrfcDetailState: LiveData<MrfcDetailState> = _mrfcDetailState

    private val _mrfcUpdateState = MutableLiveData<MrfcUpdateState>()
    val mrfcUpdateState: LiveData<MrfcUpdateState> = _mrfcUpdateState

    private val _mrfcCreateState = MutableLiveData<MrfcCreateState>()
    val mrfcCreateState: LiveData<MrfcCreateState> = _mrfcCreateState

    /**
     * Load all MRFCs
     */
    fun loadAllMrfcs(activeOnly: Boolean = true) {
        _mrfcListState.value = MrfcListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllMrfcs(activeOnly)) {
                is Result.Success -> {
                    _mrfcListState.value = MrfcListState.Success(result.data)
                }
                is Result.Error -> {
                    _mrfcListState.value = MrfcListState.Error(result.message)
                }
                is Result.Loading -> {
                    _mrfcListState.value = MrfcListState.Loading
                }
            }
        }
    }

    /**
     * Load MRFC by ID
     */
    fun loadMrfcById(id: Long) {
        _mrfcDetailState.value = MrfcDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getMrfcById(id)) {
                is Result.Success -> {
                    _mrfcDetailState.value = MrfcDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _mrfcDetailState.value = MrfcDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _mrfcDetailState.value = MrfcDetailState.Loading
                }
            }
        }
    }

    /**
     * Create new MRFC
     */
    fun createMrfc(request: CreateMrfcRequest) {
        _mrfcCreateState.value = MrfcCreateState.Loading

        viewModelScope.launch {
            when (val result = repository.createMrfc(request)) {
                is Result.Success -> {
                    _mrfcCreateState.value = MrfcCreateState.Success(result.data)
                }
                is Result.Error -> {
                    _mrfcCreateState.value = MrfcCreateState.Error(result.message)
                }
                is Result.Loading -> {
                    _mrfcCreateState.value = MrfcCreateState.Loading
                }
            }
        }
    }

    /**
     * Update MRFC
     */
    fun updateMrfc(id: Long, request: UpdateMrfcRequest) {
        _mrfcUpdateState.value = MrfcUpdateState.Loading

        viewModelScope.launch {
            when (val result = repository.updateMrfc(id, request)) {
                is Result.Success -> {
                    _mrfcUpdateState.value = MrfcUpdateState.Success(result.data)
                }
                is Result.Error -> {
                    _mrfcUpdateState.value = MrfcUpdateState.Error(result.message)
                }
                is Result.Loading -> {
                    _mrfcUpdateState.value = MrfcUpdateState.Loading
                }
            }
        }
    }

    /**
     * Refresh MRFC list
     */
    fun refresh(activeOnly: Boolean = true) {
        loadAllMrfcs(activeOnly)
    }
}

/**
 * Sealed class representing MRFC list state
 */
sealed class MrfcListState {
    object Idle : MrfcListState()
    object Loading : MrfcListState()
    data class Success(val data: List<MrfcDto>) : MrfcListState()
    data class Error(val message: String) : MrfcListState()
}

/**
 * Sealed class representing MRFC detail state
 */
sealed class MrfcDetailState {
    object Idle : MrfcDetailState()
    object Loading : MrfcDetailState()
    data class Success(val data: MrfcDto) : MrfcDetailState()
    data class Error(val message: String) : MrfcDetailState()
}

/**
 * Sealed class representing MRFC update state
 */
sealed class MrfcUpdateState {
    object Idle : MrfcUpdateState()
    object Loading : MrfcUpdateState()
    data class Success(val data: MrfcDto) : MrfcUpdateState()
    data class Error(val message: String) : MrfcUpdateState()
}

/**
 * Sealed class representing MRFC create state
 */
sealed class MrfcCreateState {
    object Idle : MrfcCreateState()
    object Loading : MrfcCreateState()
    data class Success(val data: MrfcDto) : MrfcCreateState()
    data class Error(val message: String) : MrfcCreateState()
}
