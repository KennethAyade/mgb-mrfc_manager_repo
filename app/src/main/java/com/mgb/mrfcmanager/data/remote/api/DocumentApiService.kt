package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for document operations
 */
interface DocumentApiService {

    /**
     * Upload a document (multipart)
     * Requires either mrfc_id or proponent_id
     */
    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("category") category: RequestBody,
        @Part("mrfc_id") mrfcId: RequestBody? = null,
        @Part("proponent_id") proponentId: RequestBody? = null,
        @Part("quarter_id") quarterId: RequestBody? = null,
        @Part("description") description: RequestBody? = null
    ): Response<ApiResponse<DocumentUploadResponse>>

    /**
     * Get document by ID
     */
    @GET("documents/{id}")
    suspend fun getDocumentById(
        @Path("id") id: Long
    ): Response<ApiResponse<DocumentDto>>

    /**
     * Download document (tracks download in audit log)
     */
    @GET("documents/{id}/download")
    suspend fun downloadDocument(
        @Path("id") id: Long
    ): Response<ApiResponse<Map<String, String>>>

    /**
     * Update document status (approve/reject)
     */
    @PUT("documents/{id}")
    suspend fun updateDocument(
        @Path("id") id: Long,
        @Body request: UpdateDocumentRequest
    ): Response<ApiResponse<DocumentDto>>

    /**
     * Delete a document
     */
    @DELETE("documents/{id}")
    suspend fun deleteDocument(
        @Path("id") id: Long
    ): Response<ApiResponse<Map<String, String>>>

    /**
     * Generate upload token for proponents
     */
    @POST("documents/generate-token")
    suspend fun generateUploadToken(
        @Body request: GenerateTokenRequest
    ): Response<ApiResponse<GenerateTokenResponse>>

    /**
     * Upload document via token (for proponents)
     */
    @Multipart
    @POST("documents/upload-via-token")
    suspend fun uploadViaToken(
        @Part file: MultipartBody.Part,
        @Part("token") token: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody? = null
    ): Response<ApiResponse<DocumentUploadResponse>>

    /**
     * Get documents by proponent ID
     */
    @GET("documents/proponent/{proponent_id}")
    suspend fun getDocumentsByProponent(
        @Path("proponent_id") proponentId: Long,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<DocumentDto>>>

    /**
     * Get documents by MRFC ID
     */
    @GET("documents/mrfc/{mrfc_id}")
    suspend fun getDocumentsByMrfc(
        @Path("mrfc_id") mrfcId: Long,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<DocumentDto>>>
}
