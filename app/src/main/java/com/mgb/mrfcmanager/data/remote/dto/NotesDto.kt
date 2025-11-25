package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Notes DTO for API responses
 */
data class NotesDto(
    @Json(name = "id") val id: Long,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "mrfc_id") val mrfcId: Long? = null,
    @Json(name = "quarter_id") val quarterId: Long? = null,
    @Json(name = "agenda_id") val agendaId: Long? = null,
    @Json(name = "title") val title: String?,
    @Json(name = "content") val content: String?,
    @Json(name = "is_pinned") val isPinned: Boolean = false,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String? = null
)

/**
 * Request for creating a new note
 */
data class CreateNoteRequest(
    @Json(name = "mrfc_id") val mrfcId: Long? = null,
    @Json(name = "quarter_id") val quarterId: Long? = null,
    @Json(name = "agenda_id") val agendaId: Long? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "is_pinned") val isPinned: Boolean = false
)

/**
 * Request for updating a note
 */
data class UpdateNoteRequest(
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "is_pinned") val isPinned: Boolean? = null
)

/**
 * Pagination info for paginated responses
 */
data class PaginationDto(
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "total") val total: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNext") val hasNext: Boolean,
    @Json(name = "hasPrev") val hasPrev: Boolean
)

/**
 * Paginated notes response
 */
data class PaginatedNotesResponse(
    @Json(name = "notes") val notes: List<NotesDto>,
    @Json(name = "pagination") val pagination: PaginationDto
)
