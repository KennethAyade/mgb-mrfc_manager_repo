package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.ComplianceAnalysisDto
import com.mgb.mrfcmanager.data.repository.ComplianceAnalysisRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for CMVR Compliance Analysis
 */
class ComplianceAnalysisViewModel(
    private val repository: ComplianceAnalysisRepository
) : ViewModel() {

    private val _analysisState = MutableLiveData<ComplianceAnalysisState>(ComplianceAnalysisState.Idle)
    val analysisState: LiveData<ComplianceAnalysisState> = _analysisState

    private val _updateState = MutableLiveData<UpdateAnalysisState>(UpdateAnalysisState.Idle)
    val updateState: LiveData<UpdateAnalysisState> = _updateState

    /**
     * Trigger compliance analysis for a document
     */
    fun analyzeCompliance(documentId: Long) {
        viewModelScope.launch {
            _analysisState.value = ComplianceAnalysisState.Loading
            when (val result = repository.analyzeCompliance(documentId)) {
                is Result.Success -> {
                    _analysisState.value = ComplianceAnalysisState.Success(result.data)
                }
                is Result.Error -> {
                    _analysisState.value = ComplianceAnalysisState.Error(result.message)
                }
                is Result.Loading -> {
                    _analysisState.value = ComplianceAnalysisState.Loading
                }
            }
        }
    }

    /**
     * Get existing compliance analysis for a document
     */
    fun getComplianceAnalysis(documentId: Long) {
        viewModelScope.launch {
            _analysisState.value = ComplianceAnalysisState.Loading
            when (val result = repository.getComplianceAnalysis(documentId)) {
                is Result.Success -> {
                    _analysisState.value = ComplianceAnalysisState.Success(result.data)
                }
                is Result.Error -> {
                    _analysisState.value = ComplianceAnalysisState.Error(result.message)
                }
                is Result.Loading -> {
                    _analysisState.value = ComplianceAnalysisState.Loading
                }
            }
        }
    }

    /**
     * Update compliance analysis with admin adjustments
     */
    fun updateComplianceAnalysis(
        documentId: Long,
        compliancePercentage: Double?,
        complianceRating: String?,
        adminNotes: String?
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateAnalysisState.Loading
            when (val result = repository.updateComplianceAnalysis(
                documentId,
                compliancePercentage,
                complianceRating,
                adminNotes
            )) {
                is Result.Success -> {
                    _updateState.value = UpdateAnalysisState.Success(result.data)
                    // Also update the main analysis state
                    _analysisState.value = ComplianceAnalysisState.Success(result.data)
                }
                is Result.Error -> {
                    _updateState.value = UpdateAnalysisState.Error(result.message)
                }
                is Result.Loading -> {
                    _updateState.value = UpdateAnalysisState.Loading
                }
            }
        }
    }

    /**
     * Calculate compliance rating based on percentage
     */
    fun calculateRating(percentage: Double): String {
        return when {
            percentage >= 90.0 -> "FULLY_COMPLIANT"
            percentage >= 70.0 -> "PARTIALLY_COMPLIANT"
            else -> "NON_COMPLIANT"
        }
    }

    /**
     * Get display text for compliance rating
     */
    fun getRatingDisplayText(rating: String?): String {
        return when (rating) {
            "FULLY_COMPLIANT" -> "Fully Compliant"
            "PARTIALLY_COMPLIANT" -> "Partially Compliant"
            "NON_COMPLIANT" -> "Non-Compliant"
            else -> "Unknown"
        }
    }

    /**
     * Get color resource for compliance rating
     */
    fun getRatingColor(rating: String?): String {
        return when (rating) {
            "FULLY_COMPLIANT" -> "#4CAF50" // Green
            "PARTIALLY_COMPLIANT" -> "#FF9800" // Orange
            "NON_COMPLIANT" -> "#F44336" // Red
            else -> "#9E9E9E" // Gray
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateAnalysisState.Idle
    }
}

/**
 * UI State for compliance analysis
 */
sealed class ComplianceAnalysisState {
    object Idle : ComplianceAnalysisState()
    object Loading : ComplianceAnalysisState()
    data class Success(val data: ComplianceAnalysisDto) : ComplianceAnalysisState()
    data class Error(val message: String) : ComplianceAnalysisState()
}

/**
 * UI State for updating compliance analysis
 */
sealed class UpdateAnalysisState {
    object Idle : UpdateAnalysisState()
    object Loading : UpdateAnalysisState()
    data class Success(val data: ComplianceAnalysisDto) : UpdateAnalysisState()
    data class Error(val message: String) : UpdateAnalysisState()
}

