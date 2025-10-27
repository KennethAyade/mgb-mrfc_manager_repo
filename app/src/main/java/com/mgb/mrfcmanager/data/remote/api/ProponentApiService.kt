package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Proponent Management API endpoints
 * Maps to backend /proponents routes
 */
interface ProponentApiService {

    /**
     * Get all proponents with pagination and filters
     * GET /proponents
     */
    @GET("proponents")
    suspend fun getAllProponents(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("mrfc_id") mrfcId: Long? = null,
        @Query("status") status: String? = null,
        @Query("is_active") isActive: Boolean? = null
    ): Response<ApiResponse<List<ProponentDto>>>

    /**
     * Get proponent by ID
     * GET /proponents/:id
     */
    @GET("proponents/{id}")
    suspend fun getProponentById(
        @Path("id") id: Long
    ): Response<ApiResponse<ProponentDto>>

    /**
     * Create new proponent
     * POST /proponents
     */
    @POST("proponents")
    suspend fun createProponent(
        @Body request: CreateProponentRequest
    ): Response<ApiResponse<ProponentDto>>

    /**
     * Update existing proponent
     * PUT /proponents/:id
     */
    @PUT("proponents/{id}")
    suspend fun updateProponent(
        @Path("id") id: Long,
        @Body request: CreateProponentRequest
    ): Response<ApiResponse<ProponentDto>>

    /**
     * Delete proponent (soft delete)
     * DELETE /proponents/:id
     */
    @DELETE("proponents/{id}")
    suspend fun deleteProponent(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
