package com.mgb.mrfcmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mgb.mrfcmanager.data.local.entity.NoteEntity
import com.mgb.mrfcmanager.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Notes
 * Supports offline-first operations with sync status tracking
 */
@Dao
interface NoteDao {

    // ==================== QUERIES ====================

    /**
     * Get all notes for a user (ordered by pinned first, then creation date)
     */
    @Query("""
        SELECT * FROM notes
        WHERE user_id = :userId AND sync_status != 'PENDING_DELETE'
        ORDER BY is_pinned DESC, local_created_at DESC
    """)
    fun getNotesByUser(userId: Long): Flow<List<NoteEntity>>

    /**
     * Get notes for a specific meeting/agenda
     */
    @Query("""
        SELECT * FROM notes
        WHERE user_id = :userId AND agenda_id = :agendaId AND sync_status != 'PENDING_DELETE'
        ORDER BY is_pinned DESC, local_created_at DESC
    """)
    fun getNotesByAgenda(userId: Long, agendaId: Long): Flow<List<NoteEntity>>

    /**
     * Get notes for a specific MRFC
     */
    @Query("""
        SELECT * FROM notes
        WHERE user_id = :userId AND mrfc_id = :mrfcId AND sync_status != 'PENDING_DELETE'
        ORDER BY is_pinned DESC, local_created_at DESC
    """)
    fun getNotesByMrfc(userId: Long, mrfcId: Long): Flow<List<NoteEntity>>

    /**
     * Get a single note by ID
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?

    /**
     * Get all notes pending sync
     */
    @Query("SELECT * FROM notes WHERE sync_status != 'SYNCED' ORDER BY local_created_at ASC")
    suspend fun getPendingSyncNotes(): List<NoteEntity>

    /**
     * Get count of pending sync notes
     */
    @Query("SELECT COUNT(*) FROM notes WHERE sync_status != 'SYNCED'")
    fun getPendingSyncCount(): Flow<Int>

    /**
     * Search notes by title or content
     */
    @Query("""
        SELECT * FROM notes
        WHERE user_id = :userId
        AND sync_status != 'PENDING_DELETE'
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY is_pinned DESC, local_created_at DESC
    """)
    fun searchNotes(userId: Long, query: String): Flow<List<NoteEntity>>

    // ==================== INSERT/UPDATE ====================

    /**
     * Insert a single note
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    /**
     * Insert multiple notes
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    /**
     * Update a note
     */
    @Update
    suspend fun update(note: NoteEntity)

    /**
     * Update sync status for a note
     */
    @Query("UPDATE notes SET sync_status = :status, local_updated_at = :timestamp WHERE id = :noteId")
    suspend fun updateSyncStatus(noteId: Long, status: SyncStatus, timestamp: Long = System.currentTimeMillis())

    /**
     * Update note ID after server sync (for locally created notes)
     */
    @Query("UPDATE notes SET id = :newId, sync_status = 'SYNCED' WHERE id = :oldId")
    suspend fun updateNoteId(oldId: Long, newId: Long)

    // ==================== DELETE ====================

    /**
     * Delete a note
     */
    @Delete
    suspend fun delete(note: NoteEntity)

    /**
     * Delete note by ID
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: Long)

    /**
     * Delete all notes for a user
     */
    @Query("DELETE FROM notes WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: Long)

    /**
     * Clear all synced notes (for cache refresh)
     */
    @Query("DELETE FROM notes WHERE sync_status = 'SYNCED'")
    suspend fun clearSyncedNotes()
}
