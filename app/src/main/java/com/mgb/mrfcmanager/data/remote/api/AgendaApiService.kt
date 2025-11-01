package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Agenda Management API endpoints
 * Maps to backend /agendas routes
 */
interface AgendaApiService {

    /**
     * Get all agendas with pagination and filters
     * GET /agendas
     */
    @GET("agendas")
    suspend fun getAllAgendas(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("mrfc_id") mrfcId: Long? = null,
        @Query("quarter") quarter: String? = null,
        @Query("year") year: Int? = null,
        @Query("status") status: String? = null,
        @Query("is_active") isActive: Boolean? = null
    ): Response<ApiResponse<PaginatedAgendasResponse>>

    /**
     * Get agenda by ID
     * GET /agendas/:id
     */
    @GET("agendas/{id}")
    suspend fun getAgendaById(
        @Path("id") id: Long
    ): Response<ApiResponse<AgendaDto>>

    /**
     * Create new agenda
     * POST /agendas
     */
    @POST("agendas")
    suspend fun createAgenda(
        @Body request: CreateAgendaRequest
    ): Response<ApiResponse<AgendaDto>>

    /**
     * Update existing agenda
     * PUT /agendas/:id
     */
    @PUT("agendas/{id}")
    suspend fun updateAgenda(
        @Path("id") id: Long,
        @Body request: CreateAgendaRequest
    ): Response<ApiResponse<AgendaDto>>

    /**
     * Delete agenda (soft delete)
     * DELETE /agendas/:id
     */
    @DELETE("agendas/{id}")
    suspend fun deleteAgenda(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
