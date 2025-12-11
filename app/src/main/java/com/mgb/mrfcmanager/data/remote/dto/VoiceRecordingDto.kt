package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Voice Recording Data Transfer Object
 * Maps to backend VoiceRecording model
 */
data class VoiceRecordingDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "agenda_id")
    val agendaId: Long,

    @Json(name = "recording_name")
    val recordingName: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "file_name")
    val fileName: String,

    @Json(name = "file_url")
    val fileUrl: String,

    @Json(name = "file_cloudinary_id")
    val fileCloudinaryId: String? = null,

    @Json(name = "duration")
    val duration: Int? = null,

    @Json(name = "file_size")
    val fileSize: Long? = null,

    @Json(name = "recorded_by")
    val recordedBy: Long? = null,

    @Json(name = "recorded_at")
    val recordedAt: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "recorder")
    val recorder: SimpleUserDto? = null
) {
    /**
     * Format duration from seconds to MM:SS or HH:MM:SS
     */
    val formattedDuration: String
        get() {
            val durationSeconds = duration ?: return "0:00"
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60

            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    /**
     * Format file size to human-readable format
     */
    val formattedFileSize: String
        get() {
            val size = fileSize ?: return "Unknown"
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            }
        }

    /**
     * Get recorder name or default
     */
    val recorderName: String
        get() = recorder?.fullName ?: recorder?.username ?: "Unknown"
}

/**
 * Response wrapper for voice recordings list
 */
data class VoiceRecordingsListResponse(
    @Json(name = "recordings")
    val recordings: List<VoiceRecordingDto>,

    @Json(name = "count")
    val count: Int
)

// Note: SimpleUserDto is defined in AgendaDto.kt in the same package
