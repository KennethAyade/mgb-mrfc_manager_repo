package com.mgb.mrfcmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto

/**
 * Local database entity for Meetings (Agendas)
 * Cached for offline viewing
 */
@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "mrfc_id")
    val mrfcId: Long?,

    @ColumnInfo(name = "quarter_id")
    val quarterId: Long,

    @ColumnInfo(name = "meeting_title")
    val meetingTitle: String?,

    @ColumnInfo(name = "meeting_date")
    val meetingDate: String?,

    @ColumnInfo(name = "meeting_time")
    val meetingTime: String?,

    @ColumnInfo(name = "meeting_end_time")
    val meetingEndTime: String?,

    @ColumnInfo(name = "location")
    val location: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "actual_start_time")
    val actualStartTime: String?,

    @ColumnInfo(name = "actual_end_time")
    val actualEndTime: String?,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int?,

    @ColumnInfo(name = "mrfc_name")
    val mrfcName: String?,

    @ColumnInfo(name = "quarter_name")
    val quarterName: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create entity from DTO
         */
        fun fromDto(dto: AgendaDto): MeetingEntity {
            return MeetingEntity(
                id = dto.id,
                mrfcId = dto.mrfcId,
                quarterId = dto.quarterId,
                meetingTitle = dto.meetingTitle,
                meetingDate = dto.meetingDate,
                meetingTime = dto.meetingTime,
                meetingEndTime = dto.meetingEndTime,
                location = dto.location,
                status = dto.status,
                actualStartTime = dto.actualStartTime,
                actualEndTime = dto.actualEndTime,
                durationMinutes = dto.durationMinutes,
                mrfcName = dto.mrfc?.name,
                quarterName = dto.quarter?.name,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}
