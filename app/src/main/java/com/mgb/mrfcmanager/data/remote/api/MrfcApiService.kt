package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * MRFC Management API endpoints
 * Maps to backend /mrfcs routes
 */
interface MrfcApiService {

    /**
     * Get all MRFCs with pagination and filters
     * GET /mrfcs
     */
    @GET("mrfcs")
    suspend fun getAllMrfcs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("is_active") isActive: Boolean? = null,
        @Query("location") location: String? = null
    ): Response<ApiResponse<MrfcListResponse>>

    /**
     * Get MRFC by ID
     * GET /mrfcs/:id
     */
    @GET("mrfcs/{id}")
    suspend fun getMrfcById(
        @Path("id") id: Long
    ): Response<ApiResponse<MrfcDto>>

    /**
     * Create new MRFC
     * POST /mrfcs
     * Requires admin authentication
     */
    @POST("mrfcs")
    suspend fun createMrfc(
        @Body request: CreateMrfcRequest
    ): Response<ApiResponse<MrfcDto>>

    /**
     * Update existing MRFC
     * PUT /mrfcs/:id
     * Requires admin authentication
     */
    @PUT("mrfcs/{id}")
    suspend fun updateMrfc(
        @Path("id") id: Long,
        @Body request: CreateMrfcRequest
    ): Response<ApiResponse<MrfcDto>>

    /**
     * Delete MRFC (soft delete)
     * DELETE /mrfcs/:id
     * Requires admin authentication
     */
    @DELETE("mrfcs/{id}")
    suspend fun deleteMrfc(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
