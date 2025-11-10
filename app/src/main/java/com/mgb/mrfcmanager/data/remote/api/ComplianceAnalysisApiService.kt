package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.AnalyzeComplianceRequest
import com.mgb.mrfcmanager.data.remote.dto.AnalysisProgressDto
import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.ComplianceAnalysisDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateComplianceAnalysisRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for CMVR Compliance Analysis
 */
interface ComplianceAnalysisApiService {

    /**
     * Trigger compliance analysis for a document
     * Backend will extract PDF text, analyze compliance indicators, and calculate percentage
     */
    @POST("compliance/analyze")
    suspend fun analyzeCompliance(
        @Body request: AnalyzeComplianceRequest
    ): Response<ApiResponse<ComplianceAnalysisDto>>

    /**
     * Get compliance analysis results for a document
     */
    @GET("compliance/document/{documentId}")
    suspend fun getComplianceAnalysis(
        @Path("documentId") documentId: Long
    ): Response<ApiResponse<ComplianceAnalysisDto>>

    /**
     * Update compliance analysis with admin adjustments
     */
    @PUT("compliance/document/{documentId}")
    suspend fun updateComplianceAnalysis(
        @Path("documentId") documentId: Long,
        @Body request: UpdateComplianceAnalysisRequest
    ): Response<ApiResponse<ComplianceAnalysisDto>>

    /**
     * Get all compliance analyses for a proponent
     */
    @GET("compliance/proponent/{proponentId}")
    suspend fun getProponentComplianceAnalyses(
        @Path("proponentId") proponentId: Long
    ): Response<ApiResponse<List<ComplianceAnalysisDto>>>

    /**
     * Get all compliance analyses for an MRFC
     */
    @GET("compliance/mrfc/{mrfcId}")
    suspend fun getMrfcComplianceAnalyses(
        @Path("mrfcId") mrfcId: Long
    ): Response<ApiResponse<List<ComplianceAnalysisDto>>>
    
    /**
     * Get real-time OCR analysis progress for a document
     * Returns current status, progress percentage, and current step
     */
    @GET("compliance/progress/{documentId}")
    suspend fun getAnalysisProgress(
        @Path("documentId") documentId: Long
    ): Response<ApiResponse<AnalysisProgressDto>>
}

