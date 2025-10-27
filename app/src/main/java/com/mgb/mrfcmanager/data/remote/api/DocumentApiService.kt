package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.remote.dto.DocumentUploadResponse
import com.mgb.mrfcmanager.data.remote.dto.UpdateDocumentRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for document operations
 */
interface DocumentApiService {

    /**
     * Get all documents with optional filters
     */
    @GET("documents")
    suspend fun getAllDocuments(
        @Query("mrfc_id") mrfcId: Long? = null,
        @Query("proponent_id") proponentId: Long? = null,
        @Query("agenda_id") agendaId: Long? = null,
        @Query("document_type") documentType: String? = null
    ): Response<ApiResponse<List<DocumentDto>>>

    /**
     * Get document by ID
     */
    @GET("documents/{id}")
    suspend fun getDocumentById(
        @Path("id") id: Long
    ): Response<ApiResponse<DocumentDto>>

    /**
     * Upload a document (multipart)
     */
    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("mrfc_id") mrfcId: RequestBody,
        @Part("document_type") documentType: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("proponent_id") proponentId: RequestBody? = null,
        @Part("agenda_id") agendaId: RequestBody? = null
    ): Response<ApiResponse<DocumentUploadResponse>>

    /**
     * Update document metadata
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
    ): Response<ApiResponse<Unit>>

    /**
     * Download document (returns file URL or binary data)
     */
    @GET("documents/{id}/download")
    suspend fun downloadDocument(
        @Path("id") id: Long
    ): Response<okhttp3.ResponseBody>
}
