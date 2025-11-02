package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateMrfcRequest
import com.mgb.mrfcmanager.data.remote.dto.UpdateMrfcRequest
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for MRFC operations
 * Handles data operations for MRFCs
 */
class MrfcRepository(private val mrfcApiService: MrfcApiService) {

    /**
     * Get all MRFCs with optional filtering
     */
    suspend fun getAllMrfcs(
        activeOnly: Boolean = true,
        page: Int = 1,
        limit: Int = 50
    ): Result<List<MrfcDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = mrfcApiService.getAllMrfcs(
                    page = page,
                    limit = limit,
                    isActive = if (activeOnly) true else null
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        // Extract mrfcs list from the response
                        Result.Success(body.data.mrfcs)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch MRFCs",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error. Please check your connection.",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Get MRFC by ID
     */
    suspend fun getMrfcById(id: Long): Result<MrfcDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = mrfcApiService.getMrfcById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "MRFC not found",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Create new MRFC
     */
    suspend fun createMrfc(request: CreateMrfcRequest): Result<MrfcDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = mrfcApiService.createMrfc(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create MRFC",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Update existing MRFC
     */
    suspend fun updateMrfc(id: Long, request: UpdateMrfcRequest): Result<MrfcDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = mrfcApiService.updateMrfc(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update MRFC",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Delete MRFC
     */
    suspend fun deleteMrfc(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = mrfcApiService.deleteMrfc(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete MRFC",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }
}
