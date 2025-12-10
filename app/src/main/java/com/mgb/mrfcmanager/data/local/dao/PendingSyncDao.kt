package com.mgb.mrfcmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mgb.mrfcmanager.data.local.entity.PendingSyncEntity
import com.mgb.mrfcmanager.data.local.entity.SyncEntityType
import com.mgb.mrfcmanager.data.local.entity.SyncOperation
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Pending Sync operations
 * Manages the queue of offline operations to sync when online
 */
@Dao
interface PendingSyncDao {

    // ==================== QUERIES ====================

    /**
     * Get all pending sync operations ordered by priority and creation time
     */
    @Query("SELECT * FROM pending_sync ORDER BY sync_priority DESC, created_at ASC")
    fun getAllPendingSync(): Flow<List<PendingSyncEntity>>

    /**
     * Get pending sync operations that can be retried
     */
    @Query("""
        SELECT * FROM pending_sync
        WHERE retry_count < max_retries
        ORDER BY sync_priority DESC, created_at ASC
    """)
    suspend fun getRetryablePendingSync(): List<PendingSyncEntity>

    /**
     * Get pending sync for a specific entity
     */
    @Query("""
        SELECT * FROM pending_sync
        WHERE entity_type = :entityType AND entity_id = :entityId
        LIMIT 1
    """)
    suspend fun getPendingSyncForEntity(entityType: SyncEntityType, entityId: Long): PendingSyncEntity?

    /**
     * Get count of pending sync operations
     */
    @Query("SELECT COUNT(*) FROM pending_sync")
    fun getPendingSyncCount(): Flow<Int>

    /**
     * Get pending sync by entity type
     */
    @Query("""
        SELECT * FROM pending_sync
        WHERE entity_type = :entityType
        ORDER BY sync_priority DESC, created_at ASC
    """)
    suspend fun getPendingSyncByType(entityType: SyncEntityType): List<PendingSyncEntity>

    /**
     * Check if there are any pending sync operations
     */
    @Query("SELECT COUNT(*) > 0 FROM pending_sync WHERE retry_count < max_retries")
    suspend fun hasPendingSync(): Boolean

    // ==================== INSERT/UPDATE ====================

    /**
     * Insert a pending sync operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingSync: PendingSyncEntity)

    /**
     * Update retry count and last attempted timestamp
     */
    @Query("""
        UPDATE pending_sync
        SET retry_count = retry_count + 1,
            last_attempted_at = :timestamp,
            last_error = :error
        WHERE id = :id
    """)
    suspend fun incrementRetry(id: Long, timestamp: Long = System.currentTimeMillis(), error: String? = null)

    /**
     * Update payload for a pending sync
     */
    @Query("UPDATE pending_sync SET payload = :payload WHERE id = :id")
    suspend fun updatePayload(id: Long, payload: String)

    // ==================== DELETE ====================

    /**
     * Delete a pending sync operation by ID
     */
    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Delete pending sync for a specific entity
     */
    @Query("DELETE FROM pending_sync WHERE entity_type = :entityType AND entity_id = :entityId")
    suspend fun deleteForEntity(entityType: SyncEntityType, entityId: Long)

    /**
     * Clear all pending sync operations
     */
    @Query("DELETE FROM pending_sync")
    suspend fun clearAll()

    /**
     * Clear failed sync operations (exceeded max retries)
     */
    @Query("DELETE FROM pending_sync WHERE retry_count >= max_retries")
    suspend fun clearFailedSync()
}
