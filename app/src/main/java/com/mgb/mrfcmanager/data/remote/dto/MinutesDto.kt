package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Meeting Minutes Data Transfer Object
 * Maps to backend MeetingMinutes model
 */
data class MeetingMinutesDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "summary")
    val summary: String,

    @Json(name = "decisions")
    val decisions: List<Decision> = emptyList(),

    @Json(name = "action_items")
    val actionItems: List<ActionItem> = emptyList(),

    @Json(name = "attendees_summary")
    val attendeesSummary: String? = null,

    @Json(name = "next_meeting_notes")
    val nextMeetingNotes: String? = null,

    @Json(name = "is_final")
    val isFinal: Boolean = false,

    @Json(name = "created_by")
    val createdBy: Long,

    @Json(name = "created_by_name")
    val createdByName: String,

    @Json(name = "approved_by")
    val approvedBy: Long? = null,

    @Json(name = "approved_at")
    val approvedAt: String? = null,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String
)

/**
 * Decision item within meeting minutes (JSONB in backend)
 */
data class Decision(
    @Json(name = "decision")
    val decision: String,

    @Json(name = "status")
    val status: String? = null
)

/**
 * Action item within meeting minutes (JSONB in backend)
 */
data class ActionItem(
    @Json(name = "item")
    val item: String,

    @Json(name = "assigned_to")
    val assignedTo: String? = null,

    @Json(name = "deadline")
    val deadline: String? = null
)

/**
 * Request DTO for creating/updating meeting minutes
 */
data class CreateMinutesRequest(
    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "summary")
    val summary: String,

    @Json(name = "decisions")
    val decisions: List<Decision> = emptyList(),

    @Json(name = "action_items")
    val actionItems: List<ActionItem> = emptyList(),

    @Json(name = "attendees_summary")
    val attendeesSummary: String? = null,

    @Json(name = "next_meeting_notes")
    val nextMeetingNotes: String? = null,

    @Json(name = "is_final")
    val isFinal: Boolean = false
)
