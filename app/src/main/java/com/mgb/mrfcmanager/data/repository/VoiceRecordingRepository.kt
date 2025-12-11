package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.VoiceRecordingApiService
import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingDto
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingsListResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

/**
 * Repository for voice recording operations
 */
class VoiceRecordingRepository(
    private val apiService: VoiceRecordingApiService
) {

    /**
     * Upload a new voice recording
     *
     * @param audioFile The audio file to upload
     * @param agendaId The meeting/agenda ID
     * @param recordingName The title/name of the recording
     * @param description Optional description
     * @param durationSeconds Duration in seconds
     */
    suspend fun uploadVoiceRecording(
        audioFile: File,
        agendaId: Long,
        recordingName: String,
        description: String? = null,
        durationSeconds: Int? = null
    ): Response<ApiResponse<VoiceRecordingDto>> {
        // Determine MIME type based on file extension
        val mimeType = when (audioFile.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "webm" -> "audio/webm"
            "aac" -> "audio/aac"
            "3gp" -> "audio/3gpp"
            "amr" -> "audio/amr"
            else -> "audio/mpeg" // Default to MP3
        }

        val requestFile = audioFile.asRequestBody(mimeType.toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

        val agendaIdBody = agendaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val recordingNameBody = recordingName.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
        val durationBody = durationSeconds?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.uploadVoiceRecording(
            audio = audioPart,
            agendaId = agendaIdBody,
            recordingName = recordingNameBody,
            description = descriptionBody,
            duration = durationBody
        )
    }

    /**
     * Get all voice recordings for a meeting/agenda
     *
     * @param agendaId The meeting/agenda ID
     */
    suspend fun getVoiceRecordingsByAgenda(
        agendaId: Long
    ): Response<ApiResponse<VoiceRecordingsListResponse>> {
        return apiService.getVoiceRecordingsByAgenda(agendaId)
    }

    /**
     * Get a single voice recording by ID
     *
     * @param id The voice recording ID
     */
    suspend fun getVoiceRecordingById(
        id: Long
    ): Response<ApiResponse<VoiceRecordingDto>> {
        return apiService.getVoiceRecordingById(id)
    }

    /**
     * Update voice recording metadata
     *
     * @param id The voice recording ID
     * @param recordingName New title/name
     * @param description New description
     */
    suspend fun updateVoiceRecording(
        id: Long,
        recordingName: String? = null,
        description: String? = null
    ): Response<ApiResponse<VoiceRecordingDto>> {
        val body = mutableMapOf<String, String?>()
        recordingName?.let { body["recording_name"] = it }
        if (description != null) {
            body["description"] = description
        }
        return apiService.updateVoiceRecording(id, body)
    }

    /**
     * Delete a voice recording
     *
     * @param id The voice recording ID
     */
    suspend fun deleteVoiceRecording(
        id: Long
    ): Response<ApiResponse<Any?>> {
        return apiService.deleteVoiceRecording(id)
    }
}
