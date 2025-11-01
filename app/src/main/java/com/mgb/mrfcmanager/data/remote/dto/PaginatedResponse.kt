package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Paginated response wrapper for list endpoints
 * Matches backend pagination format
 */
data class PaginatedAgendasResponse(
    @Json(name = "agendas")
    val agendas: List<AgendaDto>,

    @Json(name = "pagination")
    val pagination: PaginationInfo
)

data class PaginationInfo(
    @Json(name = "page")
    val page: Int,

    @Json(name = "limit")
    val limit: Int,

    @Json(name = "total")
    val total: Int,

    @Json(name = "totalPages")
    val totalPages: Int,

    @Json(name = "hasNext")
    val hasNext: Boolean,

    @Json(name = "hasPrev")
    val hasPrev: Boolean
)
