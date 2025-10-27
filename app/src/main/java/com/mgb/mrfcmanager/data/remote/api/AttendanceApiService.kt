package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for attendance operations
 */
interface AttendanceApiService {

    @GET("attendance")
    suspend fun getAllAttendance(
        @Query("agenda_id") agendaId: Long? = null,
        @Query("user_id") userId: Long? = null
    ): Response<ApiResponse<List<AttendanceDto>>>

    @GET("attendance/{id}")
    suspend fun getAttendanceById(
        @Path("id") id: Long
    ): Response<ApiResponse<AttendanceDto>>

    @POST("attendance")
    suspend fun createAttendance(
        @Body request: CreateAttendanceRequest
    ): Response<ApiResponse<AttendanceDto>>

    @Multipart
    @POST("attendance/{id}/photo")
    suspend fun uploadPhoto(
        @Path("id") id: Long,
        @Part photo: MultipartBody.Part
    ): Response<ApiResponse<AttendanceDto>>

    @PUT("attendance/{id}")
    suspend fun updateAttendance(
        @Path("id") id: Long,
        @Body request: CreateAttendanceRequest
    ): Response<ApiResponse<AttendanceDto>>

    @DELETE("attendance/{id}")
    suspend fun deleteAttendance(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
