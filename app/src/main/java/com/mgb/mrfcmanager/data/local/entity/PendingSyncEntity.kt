package com.mgb.mrfcmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sync operation type
 */
enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Entity type for sync operations
 */
enum class SyncEntityType {
    NOTE,
    DOCUMENT,
    ATTENDANCE
}

/**
 * Local database entity for tracking pending sync operations
 * Used to queue offline operations for later sync
 */
@Entity(
    tableName = "pending_sync",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["sync_priority"]),
        Index(value = ["created_at"])
    ]
)
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "entity_type")
    val entityType: SyncEntityType,

    @ColumnInfo(name = "entity_id")
    val entityId: Long,

    @ColumnInfo(name = "operation")
    val operation: SyncOperation,

    // JSON payload for create/update operations
    @ColumnInfo(name = "payload")
    val payload: String? = null,

    @ColumnInfo(name = "sync_priority")
    val syncPriority: Int = 0, // Higher = more urgent

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_attempted_at")
    val lastAttemptedAt: Long? = null
) {
    /**
     * Check if sync operation can be retried
     */
    fun canRetry(): Boolean = retryCount < maxRetries
}
