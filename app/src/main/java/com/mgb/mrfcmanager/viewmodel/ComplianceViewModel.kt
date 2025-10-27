package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.ComplianceDto
import com.mgb.mrfcmanager.data.remote.dto.ComplianceSummaryDto
import com.mgb.mrfcmanager.data.remote.dto.CreateComplianceRequest
import com.mgb.mrfcmanager.data.repository.ComplianceRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Compliance management
 * Handles compliance state, operations, and dashboard summary data
 */
class ComplianceViewModel(private val repository: ComplianceRepository) : ViewModel() {

    private val _complianceListState = MutableLiveData<ComplianceListState>()
    val complianceListState: LiveData<ComplianceListState> = _complianceListState

    private val _complianceDetailState = MutableLiveData<ComplianceDetailState>()
    val complianceDetailState: LiveData<ComplianceDetailState> = _complianceDetailState

    private val _complianceSummaryState = MutableLiveData<ComplianceSummaryState>()
    val complianceSummaryState: LiveData<ComplianceSummaryState> = _complianceSummaryState

    /**
     * Load all compliance records with optional filters
     */
    fun loadCompliance(
        mrfcId: Long? = null,
        proponentId: Long? = null,
        quarter: String? = null,
        year: Int? = null,
        complianceType: String? = null
    ) {
        _complianceListState.value = ComplianceListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllCompliance(mrfcId, proponentId, quarter, year, complianceType)) {
                is Result.Success -> {
                    _complianceListState.value = ComplianceListState.Success(result.data)
                }
                is Result.Error -> {
                    _complianceListState.value = ComplianceListState.Error(result.message)
                }
                is Result.Loading -> {
                    _complianceListState.value = ComplianceListState.Loading
                }
            }
        }
    }

    /**
     * Load compliance record by ID
     */
    fun loadComplianceById(id: Long) {
        _complianceDetailState.value = ComplianceDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getComplianceById(id)) {
                is Result.Success -> {
                    _complianceDetailState.value = ComplianceDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _complianceDetailState.value = ComplianceDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _complianceDetailState.value = ComplianceDetailState.Loading
                }
            }
        }
    }

    /**
     * Load compliance summary for dashboard
     */
    fun loadComplianceSummary(mrfcId: Long, quarter: String? = null, year: Int? = null) {
        _complianceSummaryState.value = ComplianceSummaryState.Loading

        viewModelScope.launch {
            when (val result = repository.getComplianceSummary(mrfcId, quarter, year)) {
                is Result.Success -> {
                    _complianceSummaryState.value = ComplianceSummaryState.Success(result.data)
                }
                is Result.Error -> {
                    _complianceSummaryState.value = ComplianceSummaryState.Error(result.message)
                }
                is Result.Loading -> {
                    _complianceSummaryState.value = ComplianceSummaryState.Loading
                }
            }
        }
    }

    /**
     * Create a new compliance record
     */
    suspend fun createCompliance(request: CreateComplianceRequest): Result<ComplianceDto> {
        return repository.createCompliance(request)
    }

    /**
     * Update a compliance record
     */
    suspend fun updateCompliance(id: Long, request: CreateComplianceRequest): Result<ComplianceDto> {
        return repository.updateCompliance(id, request)
    }

    /**
     * Delete a compliance record
     */
    suspend fun deleteCompliance(id: Long): Result<Unit> {
        return repository.deleteCompliance(id)
    }

    /**
     * Refresh compliance list
     */
    fun refreshCompliance(
        mrfcId: Long? = null,
        proponentId: Long? = null,
        quarter: String? = null,
        year: Int? = null,
        complianceType: String? = null
    ) {
        loadCompliance(mrfcId, proponentId, quarter, year, complianceType)
    }

    /**
     * Refresh compliance summary
     */
    fun refreshSummary(mrfcId: Long, quarter: String? = null, year: Int? = null) {
        loadComplianceSummary(mrfcId, quarter, year)
    }
}

/**
 * Sealed class representing compliance list state
 */
sealed class ComplianceListState {
    object Idle : ComplianceListState()
    object Loading : ComplianceListState()
    data class Success(val data: List<ComplianceDto>) : ComplianceListState()
    data class Error(val message: String) : ComplianceListState()
}

/**
 * Sealed class representing compliance detail state
 */
sealed class ComplianceDetailState {
    object Idle : ComplianceDetailState()
    object Loading : ComplianceDetailState()
    data class Success(val data: ComplianceDto) : ComplianceDetailState()
    data class Error(val message: String) : ComplianceDetailState()
}

/**
 * Sealed class representing compliance summary state
 */
sealed class ComplianceSummaryState {
    object Idle : ComplianceSummaryState()
    object Loading : ComplianceSummaryState()
    data class Success(val data: ComplianceSummaryDto) : ComplianceSummaryState()
    data class Error(val message: String) : ComplianceSummaryState()
}
