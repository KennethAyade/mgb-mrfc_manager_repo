package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Attendance DTO for API responses
 */
data class AttendanceDto(
    @Json(name = "id") val id: Long,
    @Json(name = "agenda_id") val agendaId: Long,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "user_name") val userName: String,
    @Json(name = "status") val status: String, // PRESENT, ABSENT, LATE, EXCUSED
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "time_in") val timeIn: String? = null,
    @Json(name = "time_out") val timeOut: String? = null,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String? = null
)

/**
 * Request for recording attendance
 */
data class CreateAttendanceRequest(
    @Json(name = "agenda_id") val agendaId: Long,
    @Json(name = "status") val status: String = "PRESENT",
    @Json(name = "time_in") val timeIn: String? = null,
    @Json(name = "remarks") val remarks: String? = null
)
