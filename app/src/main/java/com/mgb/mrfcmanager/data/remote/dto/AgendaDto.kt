package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Agenda Data Transfer Object
 * Maps to backend Agenda model
 */
data class AgendaDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "quarter")
    val quarter: String, // Q1, Q2, Q3, Q4

    @Json(name = "year")
    val year: Int,

    @Json(name = "agenda_number")
    val agendaNumber: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String?,

    @Json(name = "meeting_date")
    val meetingDate: String?,

    @Json(name = "status")
    val status: String, // DRAFT, FINAL, APPROVED

    @Json(name = "items")
    val items: List<AgendaItemDto>? = null,

    @Json(name = "is_active")
    val isActive: Boolean = true,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null
)

/**
 * Agenda Item DTO (individual items within an agenda)
 */
data class AgendaItemDto(
    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "agenda_id")
    val agendaId: Long? = null,

    @Json(name = "item_number")
    val itemNumber: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String?,

    @Json(name = "proponent_id")
    val proponentId: Long? = null,

    @Json(name = "duration_minutes")
    val durationMinutes: Int? = null,

    @Json(name = "order_index")
    val orderIndex: Int = 0,

    @Json(name = "status")
    val status: String = "PENDING" // PENDING, DISCUSSED, APPROVED, DEFERRED
)

/**
 * Request DTO for creating/updating Agenda
 */
data class CreateAgendaRequest(
    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "quarter")
    val quarter: String,

    @Json(name = "year")
    val year: Int,

    @Json(name = "agenda_number")
    val agendaNumber: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "meeting_date")
    val meetingDate: String? = null,

    @Json(name = "status")
    val status: String = "DRAFT",

    @Json(name = "items")
    val items: List<AgendaItemDto>? = null
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
