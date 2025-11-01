package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateMinutesRequest
import com.mgb.mrfcmanager.data.remote.dto.MeetingMinutesDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Meeting Minutes API Service
 * Maps to backend /minutes routes
 */
interface MinutesApiService {

    /**
     * Get meeting minutes for a specific agenda
     * GET /minutes/meeting/:agendaId
     */
    @GET("minutes/meeting/{agendaId}")
    suspend fun getMinutesByAgenda(
        @Path("agendaId") agendaId: Long
    ): Response<ApiResponse<MeetingMinutesDto>>

    /**
     * Get minutes by ID
     * GET /minutes/:id
     */
    @GET("minutes/{id}")
    suspend fun getMinutesById(
        @Path("id") id: Long
    ): Response<ApiResponse<MeetingMinutesDto>>

    /**
     * Create meeting minutes
     * POST /minutes
     */
    @POST("minutes")
    suspend fun createMinutes(
        @Body request: CreateMinutesRequest
    ): Response<ApiResponse<MeetingMinutesDto>>

    /**
     * Update meeting minutes
     * PUT /minutes/:id
     */
    @PUT("minutes/{id}")
    suspend fun updateMinutes(
        @Path("id") id: Long,
        @Body request: CreateMinutesRequest
    ): Response<ApiResponse<MeetingMinutesDto>>

    /**
     * Approve meeting minutes (ADMIN only)
     * PUT /minutes/:id/approve
     */
    @PUT("minutes/{id}/approve")
    suspend fun approveMinutes(
        @Path("id") id: Long
    ): Response<ApiResponse<MeetingMinutesDto>>

    /**
     * Delete meeting minutes
     * DELETE /minutes/:id
     */
    @DELETE("minutes/{id}")
    suspend fun deleteMinutes(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
