package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Document DTO for API responses
 */
data class DocumentDto(
    @Json(name = "id") val id: Long,
    @Json(name = "mrfc_id") val mrfcId: Long,
    @Json(name = "proponent_id") val proponentId: Long? = null,
    @Json(name = "agenda_id") val agendaId: Long? = null,
    @Json(name = "document_type") val documentType: String,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_path") val filePath: String,
    @Json(name = "file_size") val fileSize: Long,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "uploaded_by") val uploadedBy: Long,
    @Json(name = "description") val description: String? = null,
    @Json(name = "upload_date") val uploadDate: String,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

/**
 * Document upload response
 */
data class DocumentUploadResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_path") val filePath: String,
    @Json(name = "file_size") val fileSize: Long,
    @Json(name = "download_url") val downloadUrl: String? = null
)

/**
 * Request for updating document metadata
 */
data class UpdateDocumentRequest(
    @Json(name = "description") val description: String? = null,
    @Json(name = "document_type") val documentType: String? = null
)
