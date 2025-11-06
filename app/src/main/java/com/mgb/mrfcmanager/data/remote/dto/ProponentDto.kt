package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Proponent Data Transfer Object
 * Represents a mining/quarrying company under MRFC oversight
 */
data class ProponentDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "company_name")
    val companyName: String,

    @Json(name = "permit_number")
    val permitNumber: String?,

    @Json(name = "permit_type")
    val permitType: String?,

    @Json(name = "status")
    val status: String, // ACTIVE, INACTIVE, SUSPENDED

    @Json(name = "contact_person")
    val contactPerson: String?,

    @Json(name = "contact_number")
    val contactNumber: String?,

    @Json(name = "email")
    val email: String?,

    @Json(name = "address")
    val address: String?,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String
)

/**
 * Response wrapper for list of proponents with pagination
 */
data class ProponentListResponse(
    @Json(name = "proponents")
    val proponents: List<ProponentDto>,

    @Json(name = "pagination")
    val pagination: PaginationInfo
)

/**
 * Request body for creating a new proponent
 */
data class CreateProponentRequest(
    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "company_name")
    val companyName: String,

    @Json(name = "permit_number")
    val permitNumber: String? = null,

    @Json(name = "permit_type")
    val permitType: String? = null,

    @Json(name = "status")
    val status: String = "ACTIVE",

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
 * Request body for updating a proponent
 */
data class UpdateProponentRequest(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "company_name")
    val companyName: String? = null,

    @Json(name = "permit_number")
    val permitNumber: String? = null,

    @Json(name = "permit_type")
    val permitType: String? = null,

    @Json(name = "status")
    val status: String? = null,

    @Json(name = "contact_person")
    val contactPerson: String? = null,

    @Json(name = "contact_number")
    val contactNumber: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "address")
    val address: String? = null
)
