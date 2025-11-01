package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateMatterArisingRequest
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingDto
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingListResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Matter Arising API Service
 * Maps to backend /matters-arising routes
 */
interface MatterArisingApiService {

    /**
     * Get matters arising for a specific meeting with summary statistics
     * GET /matters-arising/meeting/:agendaId
     */
    @GET("matters-arising/meeting/{agendaId}")
    suspend fun getMattersByAgenda(
        @Path("agendaId") agendaId: Long
    ): Response<ApiResponse<MatterArisingListResponse>>

    /**
     * Create new matter arising
     * POST /matters-arising
     */
    @POST("matters-arising")
    suspend fun createMatter(
        @Body request: CreateMatterArisingRequest
    ): Response<ApiResponse<MatterArisingDto>>

    /**
     * Update matter arising
     * PUT /matters-arising/:id
     */
    @PUT("matters-arising/{id}")
    suspend fun updateMatter(
        @Path("id") id: Long,
        @Body request: CreateMatterArisingRequest
    ): Response<ApiResponse<MatterArisingDto>>

    /**
     * Delete matter arising (ADMIN only)
     * DELETE /matters-arising/:id
     */
    @DELETE("matters-arising/{id}")
    suspend fun deleteMatter(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
