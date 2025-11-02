package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Document category enum matching backend
 */
enum class DocumentCategory {
    @Json(name = "MTF_REPORT")
    MTF_REPORT,
    @Json(name = "AEPEP")
    AEPEP,
    @Json(name = "CMVR")
    CMVR,
    @Json(name = "SDMP")
    SDMP,
    @Json(name = "PRODUCTION")
    PRODUCTION,
    @Json(name = "SAFETY")
    SAFETY,
    @Json(name = "OTHER")
    OTHER;

    fun getDisplayName(): String = when (this) {
        MTF_REPORT -> "MTF Report"
        AEPEP -> "AEPEP"
        CMVR -> "CMVR"
        SDMP -> "SDMP"
        PRODUCTION -> "Production Report"
        SAFETY -> "Safety Report"
        OTHER -> "Other"
    }

    fun getDescription(): String = when (this) {
        MTF_REPORT -> "Monitoring Task Force Report"
        AEPEP -> "Annual Environment Protection and Enhancement Program"
        CMVR -> "Comprehensive Monitoring and Violation Report"
        SDMP -> "Social Development Management Program"
        PRODUCTION -> "Production and Shipment Reports"
        SAFETY -> "Safety and Health Reports"
        OTHER -> "Other Documents"
    }
}

/**
 * Document status enum matching backend
 */
enum class DocumentStatus {
    @Json(name = "PENDING")
    PENDING,
    @Json(name = "ACCEPTED")
    ACCEPTED,
    @Json(name = "REJECTED")
    REJECTED;

    fun getDisplayName(): String = when (this) {
        PENDING -> "Pending Review"
        ACCEPTED -> "Accepted"
        REJECTED -> "Rejected"
    }

    fun getColor(): Int = when (this) {
        PENDING -> android.graphics.Color.parseColor("#FF9800")
        ACCEPTED -> android.graphics.Color.parseColor("#4CAF50")
        REJECTED -> android.graphics.Color.parseColor("#F44336")
    }

    fun getIcon(): String = when (this) {
        PENDING -> "⏳"
        ACCEPTED -> "✓"
        REJECTED -> "✗"
    }
}

/**
 * Document DTO for API responses
 */
data class DocumentDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "mrfc_id")
    val mrfcId: Long? = null,

    @Json(name = "proponent_id")
    val proponentId: Long? = null,

    @Json(name = "quarter_id")
    val quarterId: Long? = null,

    @Json(name = "category")
    val category: DocumentCategory,

    @Json(name = "file_name")
    val fileName: String,

    @Json(name = "original_name")
    val originalName: String,

    @Json(name = "file_url")
    val fileUrl: String,

    @Json(name = "public_id")
    val publicId: String,

    @Json(name = "file_size")
    val fileSize: Long,

    @Json(name = "mime_type")
    val mimeType: String,

    @Json(name = "status")
    val status: DocumentStatus = DocumentStatus.PENDING,

    @Json(name = "uploaded_by")
    val uploadedBy: Long,

    @Json(name = "reviewed_by")
    val reviewedBy: Long? = null,

    @Json(name = "reviewed_at")
    val reviewedAt: String? = null,

    @Json(name = "rejection_reason")
    val rejectionReason: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String
) {
    val fileSizeDisplay: String
        get() = when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }

    val isPending: Boolean
        get() = status == DocumentStatus.PENDING

    val isAccepted: Boolean
        get() = status == DocumentStatus.ACCEPTED

    val isRejected: Boolean
        get() = status == DocumentStatus.REJECTED
}

/**
 * Document upload response
 */
data class DocumentUploadResponse(
    @Json(name = "id")
    val id: Long,

    @Json(name = "file_name")
    val fileName: String,

    @Json(name = "file_url")
    val fileUrl: String,

    @Json(name = "file_size")
    val fileSize: Long,

    @Json(name = "category")
    val category: DocumentCategory,

    @Json(name = "status")
    val status: DocumentStatus,

    @Json(name = "message")
    val message: String? = null
)

/**
 * Request for updating document (approve/reject)
 */
data class UpdateDocumentRequest(
    @Json(name = "status")
    val status: DocumentStatus,

    @Json(name = "rejection_reason")
    val rejectionReason: String? = null
)

/**
 * Generate upload token request
 */
data class GenerateTokenRequest(
    @Json(name = "proponent_id")
    val proponentId: Long,

    @Json(name = "valid_for_hours")
    val validForHours: Int = 24
)

/**
 * Generate upload token response
 */
data class GenerateTokenResponse(
    @Json(name = "token")
    val token: String,

    @Json(name = "upload_url")
    val uploadUrl: String,

    @Json(name = "expires_at")
    val expiresAt: String,

    @Json(name = "proponent_id")
    val proponentId: Long
)

/**
 * Upload via token request (multipart form data)
 */
data class UploadViaTokenRequest(
    @Json(name = "token")
    val token: String,

    @Json(name = "category")
    val category: DocumentCategory,

    @Json(name = "description")
    val description: String? = null
)
