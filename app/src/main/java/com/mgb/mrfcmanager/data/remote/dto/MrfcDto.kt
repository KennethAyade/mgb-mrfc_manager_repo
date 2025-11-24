package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Compliance Status Enum
 * Matches backend ComplianceStatus enum
 */
enum class ComplianceStatus {
    @Json(name = "COMPLIANT")
    COMPLIANT,

    @Json(name = "NON_COMPLIANT")
    NON_COMPLIANT,

    @Json(name = "PARTIAL")
    PARTIAL,

    @Json(name = "NOT_ASSESSED")
    NOT_ASSESSED;

    fun getDisplayName(): String = when (this) {
        COMPLIANT -> "Compliant"
        NON_COMPLIANT -> "Non-Compliant"
        PARTIAL -> "Partially Compliant"
        NOT_ASSESSED -> "Not Assessed"
    }

    fun getColor(): Int = when (this) {
        COMPLIANT -> android.graphics.Color.parseColor("#4CAF50") // Green
        NON_COMPLIANT -> android.graphics.Color.parseColor("#F44336") // Red
        PARTIAL -> android.graphics.Color.parseColor("#FF9800") // Orange
        NOT_ASSESSED -> android.graphics.Color.parseColor("#9E9E9E") // Gray
    }
}

/**
 * MRFC Data Transfer Object
 * Maps to backend MRFC model with compliance fields
 */
data class MrfcDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "municipality")
    val municipality: String,

    @Json(name = "province")
    val province: String?,

    @Json(name = "region")
    val region: String?,

    @Json(name = "contact_person")
    val contactPerson: String?,

    @Json(name = "contact_number")
    val contactNumber: String?,

    @Json(name = "email")
    val email: String?,

    @Json(name = "address")
    val address: String?,

    @Json(name = "is_active")
    val isActive: Boolean = true,

    @Json(name = "created_by")
    val createdBy: Long? = null,

    // Compliance fields (new)
    @Json(name = "compliance_percentage")
    val compliancePercentage: Double? = null,

    @Json(name = "compliance_status")
    val complianceStatus: ComplianceStatus? = ComplianceStatus.NOT_ASSESSED,

    @Json(name = "compliance_updated_at")
    val complianceUpdatedAt: String? = null,

    @Json(name = "compliance_updated_by")
    val complianceUpdatedBy: Long? = null,

    @Json(name = "assigned_admin_id")
    val assignedAdminId: Long? = null,

    @Json(name = "mrfc_code")
    val mrfcCode: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null
) {
    // Convenience properties for backward compatibility
    val mrfcNumber: String get() = mrfcCode ?: name
    val location: String get() = "$municipality${province?.let { ", $it" } ?: ""}"
    val chairpersonName: String get() = contactPerson ?: ""

    // Compliance helper properties
    val compliancePercentageDisplay: String
        get() = compliancePercentage?.let { String.format("%.1f%%", it) } ?: "N/A"

    val isCompliant: Boolean
        get() = complianceStatus == ComplianceStatus.COMPLIANT

    // Safe accessor for compliance status with default
    val safeComplianceStatus: ComplianceStatus
        get() = complianceStatus ?: ComplianceStatus.NOT_ASSESSED
}

/**
 * Request DTO for creating MRFC
 */
data class CreateMrfcRequest(
    @Json(name = "name")
    val name: String,

    @Json(name = "municipality")
    val municipality: String,

    @Json(name = "province")
    val province: String? = null,

    @Json(name = "region")
    val region: String? = null,

    @Json(name = "contact_person")
    val contactPerson: String? = null,

    @Json(name = "contact_number")
    val contactNumber: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "address")
    val address: String? = null,

    @Json(name = "assigned_admin_id")
    val assignedAdminId: Long? = null,

    @Json(name = "mrfc_code")
    val mrfcCode: String? = null,

    @Json(name = "assigned_user_ids")
    val assignedUserIds: List<Long>? = null
)

/**
 * Request DTO for updating MRFC
 */
data class UpdateMrfcRequest(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "municipality")
    val municipality: String? = null,

    @Json(name = "province")
    val province: String? = null,

    @Json(name = "region")
    val region: String? = null,

    @Json(name = "contact_person")
    val contactPerson: String? = null,

    @Json(name = "contact_number")
    val contactNumber: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "address")
    val address: String? = null,

    @Json(name = "assigned_admin_id")
    val assignedAdminId: Long? = null,

    @Json(name = "mrfc_code")
    val mrfcCode: String? = null,

    @Json(name = "is_active")
    val isActive: Boolean? = null
)

/**
 * Request DTO for updating MRFC compliance
 */
data class UpdateComplianceRequest(
    @Json(name = "compliance_percentage")
    val compliancePercentage: Double,

    @Json(name = "compliance_status")
    val complianceStatus: ComplianceStatus,

    @Json(name = "remarks")
    val remarks: String? = null
)

/**
 * Pagination metadata from backend (MRFC-specific format)
 */
data class MrfcPaginationDto(
    @Json(name = "current_page")
    val currentPage: Int,

    @Json(name = "total_pages")
    val totalPages: Int,

    @Json(name = "total_items")
    val totalItems: Int,

    @Json(name = "items_per_page")
    val itemsPerPage: Int
)

/**
 * Paginated list response for MRFCs
 * Matches backend response format exactly
 */
data class MrfcListResponse(
    @Json(name = "mrfcs")
    val mrfcs: List<MrfcDto>,

    @Json(name = "pagination")
    val pagination: MrfcPaginationDto
)
