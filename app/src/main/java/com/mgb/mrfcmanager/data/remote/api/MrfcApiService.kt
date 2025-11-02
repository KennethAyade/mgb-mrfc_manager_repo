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
        @Query("location") location: String? = null,
        @Query("compliance_status") complianceStatus: String? = null
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
     * Get MRFCs by proponent ID
     * GET /mrfcs/proponent/:proponent_id
     */
    @GET("mrfcs/proponent/{proponent_id}")
    suspend fun getMrfcsByProponent(
        @Path("proponent_id") proponentId: Long
    ): Response<ApiResponse<List<MrfcDto>>>

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
        @Body request: UpdateMrfcRequest
    ): Response<ApiResponse<MrfcDto>>

    /**
     * Update MRFC compliance status
     * PATCH /mrfcs/:id/compliance
     * Requires admin authentication
     */
    @PATCH("mrfcs/{id}/compliance")
    suspend fun updateCompliance(
        @Path("id") id: Long,
        @Body request: UpdateComplianceRequest
    ): Response<ApiResponse<MrfcDto>>

    /**
     * Assign admin to MRFC
     * PATCH /mrfcs/:id/assign-admin
     * Requires admin authentication
     */
    @PATCH("mrfcs/{id}/assign-admin")
    suspend fun assignAdmin(
        @Path("id") id: Long,
        @Body request: Map<String, Long>  // { "assigned_admin_id": 123 }
    ): Response<ApiResponse<MrfcDto>>

    /**
     * Delete MRFC (soft delete)
     * DELETE /mrfcs/:id
     * Requires admin authentication
     */
    @DELETE("mrfcs/{id}")
    suspend fun deleteMrfc(
        @Path("id") id: Long
    ): Response<ApiResponse<Map<String, String>>>
}
