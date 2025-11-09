package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DATA TRANSFER OBJECT: Analysis Progress
 * ========================================
 * Represents real-time OCR analysis progress
 * Used for showing progress indicator during document analysis
 */

@JsonClass(generateAdapter = true)
data class AnalysisProgressDto(
    /**
     * Current status of the analysis
     * Values: pending, processing, completed, failed, not_found
     */
    @Json(name = "status")
    val status: String,
    
    /**
     * Progress percentage (0-100)
     */
    @Json(name = "progress")
    val progress: Int,
    
    /**
     * Human-readable description of current step
     * Example: "Processing page 15/25 (75%)"
     */
    @Json(name = "current_step")
    val currentStep: String?,
    
    /**
     * Error message if analysis failed
     */
    @Json(name = "error")
    val error: String?
) {
    /**
     * Check if analysis is still in progress
     */
    fun isInProgress(): Boolean {
        return status == "pending" || status == "processing"
    }
    
    /**
     * Check if analysis completed successfully
     */
    fun isCompleted(): Boolean {
        return status == "completed"
    }
    
    /**
     * Check if analysis failed
     */
    fun isFailed(): Boolean {
        return status == "failed"
    }
    
    /**
     * Get display message for current status
     */
    fun getDisplayMessage(): String {
        return when {
            currentStep != null -> currentStep
            isCompleted() -> "Analysis complete"
            isFailed() -> error ?: "Analysis failed"
            else -> "Analyzing document..."
        }
    }
}

