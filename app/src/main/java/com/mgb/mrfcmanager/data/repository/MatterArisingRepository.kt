package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.MatterArisingApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateMatterArisingRequest
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingDto
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Matter Arising operations
 * Handles data operations for matters arising from meetings
 */
class MatterArisingRepository(private val matterArisingApiService: MatterArisingApiService) {

    /**
     * Get matters arising for a specific meeting with summary statistics
     */
    suspend fun getMattersByAgenda(agendaId: Long): Result<MatterArisingListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = matterArisingApiService.getMattersByAgenda(agendaId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch matters",
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
     * Create new matter arising
     */
    suspend fun createMatter(request: CreateMatterArisingRequest): Result<MatterArisingDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = matterArisingApiService.createMatter(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create matter",
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
     * Update existing matter arising
     */
    suspend fun updateMatter(id: Long, request: CreateMatterArisingRequest): Result<MatterArisingDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = matterArisingApiService.updateMatter(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update matter",
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
     * Delete matter arising (ADMIN only)
     */
    suspend fun deleteMatter(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = matterArisingApiService.deleteMatter(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete matter",
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
