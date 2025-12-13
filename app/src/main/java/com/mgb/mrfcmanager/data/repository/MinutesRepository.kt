package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.MinutesApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateMinutesRequest
import com.mgb.mrfcmanager.data.remote.dto.MeetingMinutesDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Meeting Minutes operations
 * Handles data operations for meeting minutes
 */
class MinutesRepository(private val minutesApiService: MinutesApiService) {

    /**
     * Get meeting minutes for a specific agenda
     */
    suspend fun getMinutesByAgenda(agendaId: Long): Result<MeetingMinutesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = minutesApiService.getMinutesByAgenda(agendaId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Minutes not found",
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
     * Get minutes by ID
     */
    suspend fun getMinutesById(id: Long): Result<MeetingMinutesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = minutesApiService.getMinutesById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Minutes not found",
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
     * Create new meeting minutes
     */
    suspend fun createMinutes(request: CreateMinutesRequest): Result<MeetingMinutesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = minutesApiService.createMinutes(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create minutes",
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
     * Update existing meeting minutes
     */
    suspend fun updateMinutes(id: Long, request: CreateMinutesRequest): Result<MeetingMinutesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = minutesApiService.updateMinutes(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update minutes",
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
     * Delete meeting minutes
     */
    suspend fun deleteMinutes(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = minutesApiService.deleteMinutes(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete minutes",
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
