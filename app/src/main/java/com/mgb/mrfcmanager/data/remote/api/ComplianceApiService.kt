package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.ComplianceDto
import com.mgb.mrfcmanager.data.remote.dto.ComplianceSummaryDto
import com.mgb.mrfcmanager.data.remote.dto.CreateComplianceRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for compliance operations
 */
interface ComplianceApiService {

    @GET("compliance")
    suspend fun getAllCompliance(
        @Query("mrfc_id") mrfcId: Long? = null,
        @Query("proponent_id") proponentId: Long? = null,
        @Query("quarter") quarter: String? = null,
        @Query("year") year: Int? = null,
        @Query("compliance_type") complianceType: String? = null
    ): Response<ApiResponse<List<ComplianceDto>>>

    @GET("compliance/{id}")
    suspend fun getComplianceById(
        @Path("id") id: Long
    ): Response<ApiResponse<ComplianceDto>>

    @GET("compliance/summary")
    suspend fun getComplianceSummary(
        @Query("mrfc_id") mrfcId: Long,
        @Query("quarter") quarter: String? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<ComplianceSummaryDto>>

    @POST("compliance")
    suspend fun createCompliance(
        @Body request: CreateComplianceRequest
    ): Response<ApiResponse<ComplianceDto>>

    @PUT("compliance/{id}")
    suspend fun updateCompliance(
        @Path("id") id: Long,
        @Body request: CreateComplianceRequest
    ): Response<ApiResponse<ComplianceDto>>

    @DELETE("compliance/{id}")
    suspend fun deleteCompliance(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
