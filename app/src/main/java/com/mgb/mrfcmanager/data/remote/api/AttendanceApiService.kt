package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.AttendanceListResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import com.mgb.mrfcmanager.data.remote.dto.UpdateAttendanceRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Attendance API Service
 * Maps to backend /attendance routes
 * Updated to match tested backend endpoints
 */
interface AttendanceApiService {

    /**
     * Get attendance records for a specific meeting with summary
     * GET /attendance/meeting/:agendaId
     */
    @GET("attendance/meeting/{agendaId}")
    suspend fun getAttendanceByMeeting(
        @Path("agendaId") agendaId: Long
    ): Response<ApiResponse<AttendanceListResponse>>

    /**
     * Record attendance for a meeting with optional photo
     * POST /attendance
     *
     * Note: For photo upload, use createAttendanceWithPhoto instead
     */
    @POST("attendance")
    suspend fun createAttendance(
        @Body request: CreateAttendanceRequest
    ): Response<ApiResponse<AttendanceDto>>

    /**
     * Record attendance with photo upload
     * POST /attendance (multipart/form-data)
     *
     * Use this for attendance with photo capture
     */
    @Multipart
    @POST("attendance")
    suspend fun createAttendanceWithPhoto(
        @Part("agenda_id") agendaId: RequestBody,
        @Part("proponent_id") proponentId: RequestBody? = null,
        @Part("attendee_name") attendeeName: RequestBody? = null,
        @Part("attendee_position") attendeePosition: RequestBody? = null,
        @Part("attendee_department") attendeeDepartment: RequestBody? = null,
        @Part("attendance_type") attendanceType: RequestBody? = null,
        @Part("is_present") isPresent: RequestBody,
        @Part("remarks") remarks: RequestBody? = null,
        @Part photo: MultipartBody.Part? = null
    ): Response<ApiResponse<AttendanceDto>>

    /**
     * Update attendance record
     * PUT /attendance/:id
     */
    @PUT("attendance/{id}")
    suspend fun updateAttendance(
        @Path("id") id: Long,
        @Body request: UpdateAttendanceRequest
    ): Response<ApiResponse<AttendanceDto>>

    /**
     * Delete attendance record
     * DELETE /attendance/:id
     */
    @DELETE("attendance/{id}")
    suspend fun deleteAttendance(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
