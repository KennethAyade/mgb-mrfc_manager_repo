package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Notes DTO for API responses
 */
data class NotesDto(
    @Json(name = "id") val id: Long,
    @Json(name = "mrfc_id") val mrfcId: Long,
    @Json(name = "agenda_id") val agendaId: Long? = null,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "note_type") val noteType: String, // MEETING, PERSONAL, ACTION_ITEM, etc.
    @Json(name = "is_private") val isPrivate: Boolean = false,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "user_name") val userName: String? = null // For display
)

/**
 * Request for creating a new note
 */
data class CreateNoteRequest(
    @Json(name = "mrfc_id") val mrfcId: Long,
    @Json(name = "agenda_id") val agendaId: Long? = null,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "note_type") val noteType: String = "MEETING",
    @Json(name = "is_private") val isPrivate: Boolean = false
)

/**
 * Request for updating a note
 */
data class UpdateNoteRequest(
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "note_type") val noteType: String? = null,
    @Json(name = "is_private") val isPrivate: Boolean? = null
)
