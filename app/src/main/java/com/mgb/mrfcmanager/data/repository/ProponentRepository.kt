package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.ProponentApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateProponentRequest
import com.mgb.mrfcmanager.data.remote.dto.ProponentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Proponent operations
 * Handles data operations for proponents
 */
class ProponentRepository(private val proponentApiService: ProponentApiService) {

    /**
     * Get all proponents with optional filtering
     */
    suspend fun getAllProponents(
        mrfcId: Long? = null,
        status: String? = null,
        activeOnly: Boolean = true,
        page: Int = 1,
        limit: Int = 50
    ): Result<List<ProponentDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = proponentApiService.getAllProponents(
                    page = page,
                    limit = limit,
                    mrfcId = mrfcId,
                    status = status,
                    isActive = if (activeOnly) true else null
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch proponents",
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
     * Get proponent by ID
     */
    suspend fun getProponentById(id: Long): Result<ProponentDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = proponentApiService.getProponentById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Proponent not found",
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
     * Create new proponent
     */
    suspend fun createProponent(request: CreateProponentRequest): Result<ProponentDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = proponentApiService.createProponent(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create proponent",
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
     * Update existing proponent
     */
    suspend fun updateProponent(id: Long, request: CreateProponentRequest): Result<ProponentDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = proponentApiService.updateProponent(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update proponent",
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
     * Delete proponent
     */
    suspend fun deleteProponent(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = proponentApiService.deleteProponent(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete proponent",
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
