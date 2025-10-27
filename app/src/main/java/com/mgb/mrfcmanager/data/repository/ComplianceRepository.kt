package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.ComplianceApiService
import com.mgb.mrfcmanager.data.remote.dto.ComplianceDto
import com.mgb.mrfcmanager.data.remote.dto.ComplianceSummaryDto
import com.mgb.mrfcmanager.data.remote.dto.CreateComplianceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for compliance operations
 * Handles all compliance-related API calls including summary/dashboard data
 */
class ComplianceRepository(private val apiService: ComplianceApiService) {

    /**
     * Get all compliance records with optional filters
     */
    suspend fun getAllCompliance(
        mrfcId: Long? = null,
        proponentId: Long? = null,
        quarter: String? = null,
        year: Int? = null,
        complianceType: String? = null
    ): Result<List<ComplianceDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllCompliance(mrfcId, proponentId, quarter, year, complianceType)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch compliance records")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Get compliance record by ID
     */
    suspend fun getComplianceById(id: Long): Result<ComplianceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getComplianceById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch compliance record")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Get compliance summary for dashboard
     */
    suspend fun getComplianceSummary(
        mrfcId: Long,
        quarter: String? = null,
        year: Int? = null
    ): Result<ComplianceSummaryDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getComplianceSummary(mrfcId, quarter, year)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch compliance summary")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Create a new compliance record
     */
    suspend fun createCompliance(request: CreateComplianceRequest): Result<ComplianceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createCompliance(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to create compliance record")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Update a compliance record
     */
    suspend fun updateCompliance(id: Long, request: CreateComplianceRequest): Result<ComplianceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateCompliance(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to update compliance record")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Delete a compliance record
     */
    suspend fun deleteCompliance(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteCompliance(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to delete compliance record")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }
}
