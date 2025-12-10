package com.mgb.mrfcmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mgb.mrfcmanager.data.remote.dto.NotesDto

/**
 * Sync status for offline operations
 */
enum class SyncStatus {
    SYNCED,          // Data is synced with server
    PENDING_CREATE,  // Created offline, needs to be uploaded
    PENDING_UPDATE,  // Updated offline, needs to be synced
    PENDING_DELETE   // Marked for deletion, needs to be synced
}

/**
 * Local database entity for Notes
 * Supports offline-first architecture with sync status tracking
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "mrfc_id")
    val mrfcId: Long? = null,

    @ColumnInfo(name = "quarter_id")
    val quarterId: Long? = null,

    @ColumnInfo(name = "agenda_id")
    val agendaId: Long? = null,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "content")
    val content: String?,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null,

    // Offline sync tracking
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    @ColumnInfo(name = "local_created_at")
    val localCreatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "local_updated_at")
    val localUpdatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convert to DTO for API calls
     */
    fun toDto(): NotesDto = NotesDto(
        id = id,
        userId = userId,
        mrfcId = mrfcId,
        quarterId = quarterId,
        agendaId = agendaId,
        title = title,
        content = content,
        isPinned = isPinned,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        /**
         * Create entity from DTO
         */
        fun fromDto(dto: NotesDto, syncStatus: SyncStatus = SyncStatus.SYNCED): NoteEntity {
            return NoteEntity(
                id = dto.id,
                userId = dto.userId,
                mrfcId = dto.mrfcId,
                quarterId = dto.quarterId,
                agendaId = dto.agendaId,
                title = dto.title,
                content = dto.content,
                isPinned = dto.isPinned,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                syncStatus = syncStatus
            )
        }

        /**
         * Generate temporary local ID for offline-created notes
         * Uses negative numbers to distinguish from server IDs
         */
        fun generateLocalId(): Long = -System.currentTimeMillis()
    }
}
