package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateProponentRequest
import com.mgb.mrfcmanager.data.remote.dto.ProponentDto
import com.mgb.mrfcmanager.data.remote.dto.ProponentListResponse
import com.mgb.mrfcmanager.data.remote.dto.UpdateProponentRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Proponent API Service
 * Handles all HTTP requests related to proponents (mining companies)
 */
interface ProponentApiService {

    /**
     * Get all proponents with pagination and filters
     * 
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 20, max: 100)
     * @param mrfcId Filter by MRFC ID (optional)
     * @param isActive Filter by active status (optional)
     * @param search Search by company name, name, contact person, or email (optional)
     * @param sortBy Sort field: company_name, created_at, contact_person (default: company_name)
     * @param sortOrder Sort order: ASC or DESC (default: ASC)
     */
    @GET("proponents")
    suspend fun getAllProponents(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("mrfc_id") mrfcId: Long? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<ApiResponse<ProponentListResponse>>

    /**
     * Get proponent by ID
     * 
     * @param id Proponent ID
     */
    @GET("proponents/{id}")
    suspend fun getProponentById(
        @Path("id") id: Long
    ): Response<ApiResponse<ProponentDto>>

    /**
     * Create new proponent
     * ADMIN only
     * 
     * @param request Create proponent request
     */
    @POST("proponents")
    suspend fun createProponent(
        @Body request: CreateProponentRequest
    ): Response<ApiResponse<ProponentDto>>

    /**
     * Update proponent
     * ADMIN only
     * 
     * @param id Proponent ID
     * @param request Update proponent request
     */
    @PUT("proponents/{id}")
    suspend fun updateProponent(
        @Path("id") id: Long,
        @Body request: UpdateProponentRequest
    ): Response<ApiResponse<ProponentDto>>

    /**
     * Delete proponent (soft delete - sets status to INACTIVE)
     * ADMIN only
     * 
     * @param id Proponent ID
     */
    @DELETE("proponents/{id}")
    suspend fun deleteProponent(
        @Path("id") id: Long
    ): Response<ApiResponse<Any?>>
}
