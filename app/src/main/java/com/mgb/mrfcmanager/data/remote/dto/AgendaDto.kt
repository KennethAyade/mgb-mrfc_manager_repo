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

    @Json(name = "meeting_date")
    val meetingDate: String?, // ISO date format: YYYY-MM-DD

    @Json(name = "meeting_time")
    val meetingTime: String? = null, // HH:MM:SS format

    @Json(name = "location")
    val location: String? = null,

    @Json(name = "status")
    val status: String, // DRAFT, PUBLISHED, COMPLETED, CANCELLED

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
    val meetingMinutes: MeetingMinutesDto? = null
)

/**
 * Agenda Item DTO (discussion topics within an agenda)
 * Updated to match backend schema
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
    val quarterId: Long,

    @Json(name = "meeting_date")
    val meetingDate: String, // YYYY-MM-DD

    @Json(name = "meeting_time")
    val meetingTime: String? = null, // HH:MM:SS

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
    val orderIndex: Int = 0
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
