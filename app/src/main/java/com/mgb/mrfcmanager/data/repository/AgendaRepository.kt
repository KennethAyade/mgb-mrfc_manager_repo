package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Agenda operations
 * Handles data operations for agendas
 */
class AgendaRepository(private val agendaApiService: AgendaApiService) {

    /**
     * Get all agendas with optional filtering
     */
    suspend fun getAllAgendas(
        mrfcId: Long? = null,
        quarter: String? = null,
        year: Int? = null,
        status: String? = null,
        activeOnly: Boolean = true,
        page: Int = 1,
        limit: Int = 50
    ): Result<List<AgendaDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaApiService.getAllAgendas(
                    page = page,
                    limit = limit,
                    mrfcId = mrfcId,
                    quarter = quarter,
                    year = year,
                    status = status,
                    isActive = if (activeOnly) true else null
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        // Extract agendas array from paginated response
                        Result.Success(body.data.agendas)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch agendas",
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
     * Get agenda by ID (with items)
     */
    suspend fun getAgendaById(id: Long): Result<AgendaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaApiService.getAgendaById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Agenda not found",
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
     * Create new agenda
     */
    suspend fun createAgenda(request: CreateAgendaRequest): Result<AgendaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaApiService.createAgenda(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create agenda",
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
     * Update existing agenda
     */
    suspend fun updateAgenda(id: Long, request: CreateAgendaRequest): Result<AgendaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaApiService.updateAgenda(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update agenda",
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
     * Delete agenda
     */
    suspend fun deleteAgenda(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaApiService.deleteAgenda(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete agenda",
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
