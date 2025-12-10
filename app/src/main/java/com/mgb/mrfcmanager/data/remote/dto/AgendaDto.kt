package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Agenda Data Transfer Object
 * Maps to backend Agenda model - represents MRFC meeting agendas
 * Updated to match backend schema exactly
 */
data class AgendaDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "mrfc_id")
    val mrfcId: Long?, // null for general meetings not tied to specific MRFC

    @Json(name = "quarter_id")
    val quarterId: Long,

    @Json(name = "meeting_title")
    val meetingTitle: String? = null,

    @Json(name = "meeting_date")
    val meetingDate: String?, // ISO date format: YYYY-MM-DD

    @Json(name = "meeting_time")
    val meetingTime: String? = null, // Start time e.g., "09:00 AM"

    @Json(name = "meeting_end_time")
    val meetingEndTime: String? = null, // End time e.g., "05:00 PM"

    @Json(name = "location")
    val location: String? = null,

    @Json(name = "status")
    val status: String, // DRAFT, PROPOSED, PUBLISHED, COMPLETED, CANCELLED

    @Json(name = "proposed_by")
    val proposedBy: Long? = null,

    @Json(name = "proposed_at")
    val proposedAt: String? = null,

    @Json(name = "approved_by")
    val approvedBy: Long? = null,

    @Json(name = "approved_at")
    val approvedAt: String? = null,

    @Json(name = "denied_by")
    val deniedBy: Long? = null,

    @Json(name = "denied_at")
    val deniedAt: String? = null,

    @Json(name = "denial_remarks")
    val denialRemarks: String? = null,

    @Json(name = "actual_start_time")
    val actualStartTime: String? = null,

    @Json(name = "actual_end_time")
    val actualEndTime: String? = null,

    @Json(name = "duration_minutes")
    val durationMinutes: Int? = null,

    @Json(name = "started_by")
    val startedBy: Long? = null,

    @Json(name = "ended_by")
    val endedBy: Long? = null,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String,

    // Populated by backend when including related data
    @Json(name = "mrfc")
    val mrfc: MrfcDto? = null,

    @Json(name = "quarter")
    val quarter: QuarterDto? = null,

    @Json(name = "agenda_items")
    val agendaItems: List<AgendaItemDto>? = null,

    @Json(name = "meeting_minutes")
    val meetingMinutes: MeetingMinutesDto? = null,

    @Json(name = "proposed_by_user")
    val proposedByUser: SimpleUserDto? = null
) {
    // Helper property for UI
    val mrfcName: String?
        get() = mrfc?.name

    // Helper property for proposed by user name
    val proposedByName: String?
        get() = proposedByUser?.fullName
}

/**
 * Agenda Item DTO (discussion topics within an agenda)
 * Updated to match backend schema with proposal workflow
 */
data class AgendaItemDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "added_by")
    val addedBy: Long,

    @Json(name = "added_by_name")
    val addedByName: String,

    @Json(name = "added_by_username")
    val addedByUsername: String,

    @Json(name = "order_index")
    val orderIndex: Int = 0,

    @Json(name = "status")
    val status: String = "APPROVED", // PROPOSED, APPROVED, DENIED

    @Json(name = "approved_by")
    val approvedBy: Long? = null,

    @Json(name = "approved_at")
    val approvedAt: String? = null,

    @Json(name = "denied_by")
    val deniedBy: Long? = null,

    @Json(name = "denied_at")
    val deniedAt: String? = null,

    @Json(name = "denial_remarks")
    val denialRemarks: String? = null,

    @Json(name = "mrfc_id")
    val mrfcId: Long? = null,

    @Json(name = "proponent_id")
    val proponentId: Long? = null,

    @Json(name = "file_category")
    val fileCategory: String? = null,

    // Feature 2: Other Matters tab - items added after agenda is finalized
    @Json(name = "is_other_matter")
    val isOtherMatter: Boolean = false,

    // Feature 7: Highlight discussed items (green background)
    @Json(name = "is_highlighted")
    val isHighlighted: Boolean = false,

    @Json(name = "highlighted_by")
    val highlightedBy: Long? = null,

    @Json(name = "highlighted_at")
    val highlightedAt: String? = null,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String
)

/**
 * Quarter DTO for quarter references
 * Represents a quarterly period for MRFC meetings and document submissions
 */
data class QuarterDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "name")
    val name: String, // "Q1 2025", "Q2 2025", etc.

    @Json(name = "year")
    val year: Int,

    @Json(name = "quarter_number")
    val quarterNumber: Int, // 1, 2, 3, or 4

    @Json(name = "start_date")
    val startDate: String,

    @Json(name = "end_date")
    val endDate: String,

    @Json(name = "is_current")
    val isCurrent: Boolean = false
)

/**
 * Request DTO for creating/updating Agenda
 */
data class CreateAgendaRequest(
    @Json(name = "mrfc_id")
    val mrfcId: Long? = null, // null means general meeting (not tied to specific MRFC)

    @Json(name = "quarter_id")
    val quarterId: Long? = null, // Can use quarterId OR (quarterNumber + year)

    @Json(name = "quarter_number")
    val quarterNumber: Int? = null, // 1, 2, 3, or 4

    @Json(name = "year")
    val year: Int? = null, // e.g., 2025

    @Json(name = "meeting_title")
    val meetingTitle: String? = null,

    @Json(name = "meeting_date")
    val meetingDate: String, // YYYY-MM-DD

    @Json(name = "meeting_time")
    val meetingTime: String? = null, // Start time (e.g., "09:00 AM")

    @Json(name = "meeting_end_time")
    val meetingEndTime: String? = null, // End time (e.g., "05:00 PM")

    @Json(name = "location")
    val location: String? = null,

    @Json(name = "status")
    val status: String = "DRAFT"
)

/**
 * Request DTO for creating/updating Agenda Item
 */
data class CreateAgendaItemRequest(
    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "order_index")
    val orderIndex: Int = 0,

    @Json(name = "mrfc_id")
    val mrfcId: Long? = null,

    @Json(name = "proponent_id")
    val proponentId: Long? = null,

    @Json(name = "file_category")
    val fileCategory: String? = null,

    // Feature 2: Mark as other matter
    @Json(name = "is_other_matter")
    val isOtherMatter: Boolean = false
)

/**
 * Request DTO for marking other matter status
 */
data class MarkOtherMatterRequest(
    @Json(name = "is_other_matter")
    val isOtherMatter: Boolean
)

/**
 * Simple User DTO for nested responses
 */
data class SimpleUserDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "username")
    val username: String,

    @Json(name = "full_name")
    val fullName: String
)

/**
 * Deny Proposal Request
 */
data class DenyProposalRequest(
    @Json(name = "denial_remarks")
    val denialRemarks: String
)

/**
 * Agenda list response with pagination
 */
data class AgendaListResponse(
    @Json(name = "agendas")
    val agendas: List<AgendaDto>,

    @Json(name = "total")
    val total: Int,

    @Json(name = "page")
    val page: Int,

    @Json(name = "limit")
    val limit: Int
)

/**
 * Meeting Timer Response
 * Response from start/end meeting endpoints
 */
data class MeetingTimerResponse(
    @Json(name = "id")
    val id: Long,

    @Json(name = "actual_start_time")
    val actualStartTime: String? = null,

    @Json(name = "actual_end_time")
    val actualEndTime: String? = null,

    @Json(name = "duration_minutes")
    val durationMinutes: Int? = null,

    @Json(name = "started_by")
    val startedBy: Long? = null,

    @Json(name = "ended_by")
    val endedBy: Long? = null
)
