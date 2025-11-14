package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.api.ItemOrderUpdate
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Agenda Item operations
 * Handles data operations for discussion topics within meetings
 */
class AgendaItemRepository(private val agendaItemApiService: AgendaItemApiService) {

    /**
     * Get all agenda items for a specific meeting
     */
    suspend fun getItemsByAgenda(agendaId: Long): Result<List<AgendaItemDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.getItemsByAgenda(agendaId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch agenda items",
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
     * Get agenda item by ID
     */
    suspend fun getItemById(id: Long): Result<AgendaItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.getItemById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Agenda item not found",
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
     * Create new agenda item (discussion topic)
     */
    suspend fun createItem(request: CreateAgendaItemRequest): Result<AgendaItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.createItem(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create agenda item",
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
     * Update existing agenda item
     */
    suspend fun updateItem(id: Long, request: CreateAgendaItemRequest): Result<AgendaItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.updateItem(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update agenda item",
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
     * Reorder agenda items
     */
    suspend fun reorderItems(items: List<ItemOrderUpdate>): Result<List<AgendaItemDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.reorderItems(items)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to reorder items",
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
     * Delete agenda item
     */
    suspend fun deleteItem(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.deleteItem(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete agenda item",
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
     * Get current user's proposed items (all statuses)
     */
    suspend fun getMyProposals(): Result<List<AgendaItemDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.getMyProposals()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch proposals",
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
     * Approve a proposed agenda item (ADMIN only)
     */
    suspend fun approveItem(itemId: Long): Result<AgendaItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = agendaItemApiService.approveItem(itemId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to approve item",
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
     * Deny a proposed agenda item with remarks (ADMIN only)
     */
    suspend fun denyItem(itemId: Long, remarks: String): Result<AgendaItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                val request = com.mgb.mrfcmanager.data.remote.dto.DenyProposalRequest(denialRemarks = remarks)
                val response = agendaItemApiService.denyItem(itemId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to deny item",
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
