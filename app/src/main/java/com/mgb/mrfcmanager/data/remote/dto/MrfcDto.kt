package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * MRFC Data Transfer Object
 * Maps to backend MRFC model exactly
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

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null
) {
    // Convenience properties for backward compatibility
    val mrfcNumber: String get() = name
    val location: String get() = "$municipality${province?.let { ", $it" } ?: ""}"
    val chairpersonName: String get() = contactPerson ?: ""
}

/**
 * Request DTO for creating/updating MRFC
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
    val address: String? = null
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
