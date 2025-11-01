package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Matter Arising Data Transfer Object
 * Maps to backend MatterArising model
 * Represents unresolved issues from previous meetings requiring follow-up
 */
data class MatterArisingDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "issue")
    val issue: String,

    @Json(name = "status")
    val status: String, // PENDING, IN_PROGRESS, RESOLVED

    @Json(name = "assigned_to")
    val assignedTo: String? = null,

    @Json(name = "date_raised")
    val dateRaised: String, // YYYY-MM-DD format

    @Json(name = "date_resolved")
    val dateResolved: String? = null, // YYYY-MM-DD format

    @Json(name = "remarks")
    val remarks: String? = null,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String
)

/**
 * Request DTO for creating/updating matter arising
 */
data class CreateMatterArisingRequest(
    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "issue")
    val issue: String,

    @Json(name = "assigned_to")
    val assignedTo: String? = null,

    @Json(name = "date_raised")
    val dateRaised: String, // YYYY-MM-DD

    @Json(name = "status")
    val status: String = "PENDING",

    @Json(name = "date_resolved")
    val dateResolved: String? = null,

    @Json(name = "remarks")
    val remarks: String? = null
)

/**
 * Response with list of matters and summary statistics
 */
data class MatterArisingListResponse(
    @Json(name = "matters")
    val matters: List<MatterArisingDto>,

    @Json(name = "summary")
    val summary: MatterArisingSummary
)

/**
 * Summary statistics for matters arising
 */
data class MatterArisingSummary(
    @Json(name = "total")
    val total: Int,

    @Json(name = "pending")
    val pending: Int,

    @Json(name = "in_progress")
    val inProgress: Int,

    @Json(name = "resolved")
    val resolved: Int,

    @Json(name = "resolution_rate")
    val resolutionRate: Double
)
