package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.ComplianceAnalysisApiService
import com.mgb.mrfcmanager.data.remote.dto.AnalyzeComplianceRequest
import com.mgb.mrfcmanager.data.remote.dto.AnalysisProgressDto
import com.mgb.mrfcmanager.data.remote.dto.ComplianceAnalysisDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateComplianceAnalysisRequest
import org.json.JSONObject

/**
 * Repository for CMVR Compliance Analysis operations
 */
class ComplianceAnalysisRepository(
    private val apiService: ComplianceAnalysisApiService
) {

    /**
     * Trigger compliance analysis for a document
     */
    suspend fun analyzeCompliance(documentId: Long): Result<ComplianceAnalysisDto> {
        return try {
            android.util.Log.d("ComplianceRepo", "Calling analyze API for document $documentId")
            val response = apiService.analyzeCompliance(AnalyzeComplianceRequest(documentId))
            android.util.Log.d("ComplianceRepo", "API Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    val body = apiResponse.data
                    android.util.Log.d("ComplianceRepo", "✅ Success! Parsed DTO: id=${body.id}, docId=${body.documentId}, percentage=${body.compliancePercentage}")
                    Result.Success(body)
                } else {
                    val errorMsg = apiResponse.message ?: apiResponse.error?.message ?: "Analysis failed"
                    android.util.Log.e("ComplianceRepo", "❌ API returned success=false: $errorMsg")
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Feature not available yet. Backend API not implemented."
                    400 -> parseErrorMessage(response.errorBody()?.string()) ?: "Invalid request"
                    401 -> "Please login again"
                    403 -> "Permission denied"
                    500 -> "Server error. Please try again later."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Analysis failed"
                }
                Result.Error(errorMsg)
            }
        } catch (e: com.squareup.moshi.JsonDataException) {
            // Moshi parsing error - backend returned unexpected format
            android.util.Log.e("ComplianceRepo", "❌ JsonDataException: ${e.message}", e)
            Result.Error("Server returned invalid data. Please try again.")
        } catch (e: com.squareup.moshi.JsonEncodingException) {
            // Moshi encoding error
            android.util.Log.e("ComplianceRepo", "❌ JsonEncodingException: ${e.message}", e)
            Result.Error("Data format error. Please contact support.")
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("ComplianceRepo", "❌ SocketTimeoutException: ${e.message}", e)
            Result.Error("Request timed out. Check your connection.")
        } catch (e: java.io.IOException) {
            android.util.Log.e("ComplianceRepo", "❌ IOException: ${e.message}", e)
            Result.Error("Network error. Please check your connection.")
        } catch (e: Exception) {
            android.util.Log.e("ComplianceRepo", "❌ Exception: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.Error("Analysis failed: ${e.message ?: "Unknown error"}")
        }
    }

    /**
     * Get compliance analysis results for a document
     */
    suspend fun getComplianceAnalysis(documentId: Long): Result<ComplianceAnalysisDto> {
        return try {
            val response = apiService.getComplianceAnalysis(documentId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.Success(apiResponse.data)
                } else {
                    Result.Error(apiResponse.message ?: apiResponse.error?.message ?: "Failed to get analysis")
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "No compliance analysis found for this document."
                    401 -> "Authentication required. Please login again."
                    403 -> "You don't have permission to view this analysis."
                    500 -> "Server error occurred. Please try again later."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Failed to get compliance analysis"
                }
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error occurred")
        }
    }

    /**
     * Update compliance analysis with admin adjustments
     */
    suspend fun updateComplianceAnalysis(
        documentId: Long,
        compliancePercentage: Double?,
        complianceRating: String?,
        adminNotes: String?
    ): Result<ComplianceAnalysisDto> {
        return try {
            val request = UpdateComplianceAnalysisRequest(
                documentId = documentId,
                compliancePercentage = compliancePercentage,
                complianceRating = complianceRating,
                adminNotes = adminNotes,
                adminAdjusted = true
            )
            val response = apiService.updateComplianceAnalysis(documentId, request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.Success(apiResponse.data)
                } else {
                    Result.Error(apiResponse.message ?: apiResponse.error?.message ?: "Failed to update")
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Compliance analysis not found."
                    400 -> parseErrorMessage(response.errorBody()?.string()) ?: "Invalid data provided"
                    401 -> "Authentication required. Please login again."
                    403 -> "You don't have permission to update this analysis."
                    500 -> "Server error occurred. Please try again later."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Failed to update compliance analysis"
                }
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error occurred")
        }
    }

    /**
     * Get all compliance analyses for a proponent
     */
    suspend fun getProponentComplianceAnalyses(proponentId: Long): Result<List<ComplianceAnalysisDto>> {
        return try {
            val response = apiService.getProponentComplianceAnalyses(proponentId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.Success(apiResponse.data)
                } else {
                    Result.Error(apiResponse.message ?: "Failed to get analyses")
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to get compliance analyses"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error occurred")
        }
    }

    /**
     * Get all compliance analyses for an MRFC
     */
    suspend fun getMrfcComplianceAnalyses(mrfcId: Long): Result<List<ComplianceAnalysisDto>> {
        return try {
            val response = apiService.getMrfcComplianceAnalyses(mrfcId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.Success(apiResponse.data)
                } else {
                    Result.Error(apiResponse.message ?: "Failed to get analyses")
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "No compliance analyses found for this MRFC."
                    401 -> "Authentication required. Please login again."
                    403 -> "You don't have permission to view these analyses."
                    500 -> "Server error occurred. Please try again later."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Failed to get compliance analyses"
                }
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error occurred")
        }
    }

    /**
     * Get real-time OCR analysis progress for a document
     */
    suspend fun getAnalysisProgress(documentId: Long): Result<AnalysisProgressDto> {
        return try {
            val response = apiService.getAnalysisProgress(documentId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.Success(apiResponse.data)
                } else {
                    Result.Error(apiResponse.message ?: "Failed to get progress")
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Progress information not available."
                    401 -> "Authentication required."
                    else -> "Failed to get progress"
                }
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Network error occurred")
        }
    }

    /**
     * Force re-analysis of a document
     * Deletes cached results and triggers fresh analysis
     */
    suspend fun reanalyzeCompliance(documentId: Long): Result<ComplianceAnalysisDto> {
        return try {
            android.util.Log.d("ComplianceRepo", "🔄 Calling reanalyze API for document $documentId")
            val response = apiService.reanalyzeCompliance(documentId)
            android.util.Log.d("ComplianceRepo", "API Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    android.util.Log.d("ComplianceRepo", "✅ Reanalysis started successfully")
                    Result.Success(apiResponse.data)
                } else {
                    val errorMsg = apiResponse.message ?: apiResponse.error?.message ?: "Reanalysis failed"
                    android.util.Log.e("ComplianceRepo", "❌ API returned success=false: $errorMsg")
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Document not found"
                    400 -> parseErrorMessage(response.errorBody()?.string()) ?: "Invalid request"
                    401 -> "Please login again"
                    403 -> "Permission denied"
                    500 -> "Server error. Please try again later."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Reanalysis failed"
                }
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            android.util.Log.e("ComplianceRepo", "❌ Exception: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.Error("Reanalysis failed: ${e.message ?: "Network error occurred"}")
        }
    }

    /**
     * Parse error message from JSON response
     */
    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            if (errorBody != null) {
                val jsonError = JSONObject(errorBody)
                // Try to extract message from various error formats
                when {
                    jsonError.has("error") -> {
                        val error = jsonError.getJSONObject("error")
                        error.optString("message", null)
                    }
                    jsonError.has("message") -> jsonError.getString("message")
                    else -> null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

