package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Attendance DTO for API responses
 * Maps to backend Attendance model
 * Updated to match tested backend schema
 */
data class AttendanceDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "proponent_id")
    val proponentId: Long? = null,

    @Json(name = "attendee_name")
    val attendeeName: String? = null,

    @Json(name = "attendee_position")
    val attendeePosition: String? = null,

    @Json(name = "attendee_department")
    val attendeeDepartment: String? = null,

    @Json(name = "is_present")
    val isPresent: Boolean = false,

    @Json(name = "photo_url")
    val photoUrl: String? = null,

    @Json(name = "photo_cloudinary_id")
    val photoCloudinaryId: String? = null,

    @Json(name = "marked_at")
    val markedAt: String,

    @Json(name = "marked_by")
    val markedBy: Long? = null,

    @Json(name = "remarks")
    val remarks: String? = null,

    // Populated by backend when including related data
    @Json(name = "proponent")
    val proponent: ProponentDto? = null,

    @Json(name = "marker")
    val marker: UserDto? = null,

    // Tablet number assigned based on order of attendance logging
    @Json(name = "tablet_number")
    val tabletNumber: Int? = null
)

/**
 * Request for recording attendance
 * Either proponent_id OR attendee_name is required
 */
data class CreateAttendanceRequest(
    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "proponent_id")
    val proponentId: Long? = null,

    @Json(name = "attendee_name")
    val attendeeName: String? = null,

    @Json(name = "attendee_position")
    val attendeePosition: String? = null,

    @Json(name = "attendee_department")
    val attendeeDepartment: String? = null,

    @Json(name = "is_present")
    val isPresent: Boolean = true,

    @Json(name = "remarks")
    val remarks: String? = null
)

/**
 * Attendance list response with summary statistics
 */
data class AttendanceListResponse(
    @Json(name = "attendance")
    val attendance: List<AttendanceDto>,

    @Json(name = "summary")
    val summary: AttendanceSummary,

    @Json(name = "current_user_logged")
    val currentUserLogged: Boolean = false
)

/**
 * Attendance summary statistics
 */
data class AttendanceSummary(
    @Json(name = "total_attendees")
    val totalAttendees: Int,

    @Json(name = "present")
    val present: Int,

    @Json(name = "absent")
    val absent: Int,

    @Json(name = "attendance_rate")
    val attendanceRate: Double
)
