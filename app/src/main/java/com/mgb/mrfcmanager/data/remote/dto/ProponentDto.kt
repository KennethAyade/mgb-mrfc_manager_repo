package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Proponent Data Transfer Object
 * Maps to backend Proponent model
 */
data class ProponentDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "contact_number")
    val contactNumber: String,

    @Json(name = "email")
    val email: String?,

    @Json(name = "address")
    val address: String?,

    @Json(name = "project_title")
    val projectTitle: String,

    @Json(name = "project_description")
    val projectDescription: String?,

    @Json(name = "amount_requested")
    val amountRequested: Double,

    @Json(name = "status")
    val status: String, // PENDING, APPROVED, REJECTED, etc.

    @Json(name = "submitted_date")
    val submittedDate: String?,

    @Json(name = "is_active")
    val isActive: Boolean = true,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null
)

/**
 * Request DTO for creating/updating Proponent
 */
data class CreateProponentRequest(
    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "contact_number")
    val contactNumber: String,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "address")
    val address: String? = null,

    @Json(name = "project_title")
    val projectTitle: String,

    @Json(name = "project_description")
    val projectDescription: String? = null,

    @Json(name = "amount_requested")
    val amountRequested: Double,

    @Json(name = "status")
    val status: String = "PENDING"
)

/**
 * Proponent list response with pagination
 */
data class ProponentListResponse(
    @Json(name = "proponents")
    val proponents: List<ProponentDto>,

    @Json(name = "total")
    val total: Int,

    @Json(name = "page")
    val page: Int,

    @Json(name = "limit")
    val limit: Int
)
