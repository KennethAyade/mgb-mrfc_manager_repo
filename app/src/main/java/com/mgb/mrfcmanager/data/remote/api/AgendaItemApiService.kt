package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaItemRequest
import com.mgb.mrfcmanager.data.remote.dto.MarkOtherMatterRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Agenda Item API Service
 * Maps to backend /agenda-items routes
 * Handles discussion topics within meetings
 */
interface AgendaItemApiService {

    /**
     * Get all agenda items for a specific meeting
     * GET /agenda-items/agenda/:agendaId
     */
    @GET("agenda-items/agenda/{agendaId}")
    suspend fun getItemsByAgenda(
        @Path("agendaId") agendaId: Long
    ): Response<ApiResponse<List<AgendaItemDto>>>

    /**
     * Get agenda item by ID
     * GET /agenda-items/:id
     */
    @GET("agenda-items/{id}")
    suspend fun getItemById(
        @Path("id") id: Long
    ): Response<ApiResponse<AgendaItemDto>>

    /**
     * Create new agenda item (discussion topic)
     * POST /agenda-items
     */
    @POST("agenda-items")
    suspend fun createItem(
        @Body request: CreateAgendaItemRequest
    ): Response<ApiResponse<AgendaItemDto>>

    /**
     * Update agenda item
     * PUT /agenda-items/:id
     */
    @PUT("agenda-items/{id}")
    suspend fun updateItem(
        @Path("id") id: Long,
        @Body request: CreateAgendaItemRequest
    ): Response<ApiResponse<AgendaItemDto>>

    /**
     * Reorder agenda items
     * PUT /agenda-items/reorder
     */
    @PUT("agenda-items/reorder")
    suspend fun reorderItems(
        @Body items: List<ItemOrderUpdate>
    ): Response<ApiResponse<List<AgendaItemDto>>>

    /**
     * Delete agenda item
     * DELETE /agenda-items/:id
     */
    @DELETE("agenda-items/{id}")
    suspend fun deleteItem(
        @Path("id") id: Long
    ): Response<Any>

    /**
     * Get current user's proposed items (all statuses)
     * GET /agenda-items/my-proposals
     */
    @GET("agenda-items/my-proposals")
    suspend fun getMyProposals(): Response<ApiResponse<List<AgendaItemDto>>>

    /**
     * Approve a proposed item (ADMIN only)
     * POST /agenda-items/:id/approve
     */
    @POST("agenda-items/{id}/approve")
    suspend fun approveItem(
        @Path("id") id: Long
    ): Response<ApiResponse<AgendaItemDto>>

    /**
     * Deny a proposed item with remarks (ADMIN only)
     * POST /agenda-items/:id/deny
     */
    @POST("agenda-items/{id}/deny")
    suspend fun denyItem(
        @Path("id") id: Long,
        @Body request: com.mgb.mrfcmanager.data.remote.dto.DenyProposalRequest
    ): Response<ApiResponse<AgendaItemDto>>

    /**
     * Get "Other Matters" items for a meeting
     * GET /agenda-items/meeting/:agendaId/other-matters
     */
    @GET("agenda-items/meeting/{agendaId}/other-matters")
    suspend fun getOtherMatters(
        @Path("agendaId") agendaId: Long
    ): Response<ApiResponse<List<AgendaItemDto>>>

    /**
     * Toggle highlight status on an agenda item (ADMIN only)
     * POST /agenda-items/:id/toggle-highlight
     */
    @POST("agenda-items/{id}/toggle-highlight")
    suspend fun toggleHighlight(
        @Path("id") id: Long
    ): Response<ApiResponse<AgendaItemDto>>

    /**
     * Mark/unmark an agenda item as "Other Matter" (ADMIN only)
     * POST /agenda-items/:id/mark-other-matter
     */
    @POST("agenda-items/{id}/mark-other-matter")
    suspend fun markOtherMatter(
        @Path("id") id: Long,
        @Body request: MarkOtherMatterRequest
    ): Response<ApiResponse<AgendaItemDto>>
}

/**
 * Helper data class for reordering items
 */
data class ItemOrderUpdate(
    val id: Long,
    val order_index: Int
)
