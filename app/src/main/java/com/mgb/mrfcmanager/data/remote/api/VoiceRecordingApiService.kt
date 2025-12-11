package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingDto
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingsListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Voice Recording API Service
 * Handles all voice recording related API calls
 */
interface VoiceRecordingApiService {

    /**
     * Upload a new voice recording
     * POST /voice-recordings/upload
     *
     * @param audio The audio file
     * @param agendaId The meeting/agenda ID
     * @param recordingName The title/name of the recording
     * @param description Optional description
     * @param duration Duration in seconds
     */
    @Multipart
    @POST("voice-recordings/upload")
    suspend fun uploadVoiceRecording(
        @Part audio: MultipartBody.Part,
        @Part("agenda_id") agendaId: RequestBody,
        @Part("recording_name") recordingName: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("duration") duration: RequestBody? = null
    ): Response<ApiResponse<VoiceRecordingDto>>

    /**
     * Get all voice recordings for a meeting/agenda
     * GET /voice-recordings/agenda/:agendaId
     *
     * @param agendaId The meeting/agenda ID
     */
    @GET("voice-recordings/agenda/{agendaId}")
    suspend fun getVoiceRecordingsByAgenda(
        @Path("agendaId") agendaId: Long
    ): Response<ApiResponse<VoiceRecordingsListResponse>>

    /**
     * Get a single voice recording by ID
     * GET /voice-recordings/:id
     *
     * @param id The voice recording ID
     */
    @GET("voice-recordings/{id}")
    suspend fun getVoiceRecordingById(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceRecordingDto>>

    /**
     * Update voice recording metadata
     * PUT /voice-recordings/:id
     *
     * @param id The voice recording ID
     * @param body Update request with new name/description
     */
    @PUT("voice-recordings/{id}")
    suspend fun updateVoiceRecording(
        @Path("id") id: Long,
        @Body body: Map<String, String?>
    ): Response<ApiResponse<VoiceRecordingDto>>

    /**
     * Delete a voice recording
     * DELETE /voice-recordings/:id
     *
     * @param id The voice recording ID
     */
    @DELETE("voice-recordings/{id}")
    suspend fun deleteVoiceRecording(
        @Path("id") id: Long
    ): Response<ApiResponse<Any?>>
}
