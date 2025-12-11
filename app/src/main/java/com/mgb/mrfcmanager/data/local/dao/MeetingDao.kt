package com.mgb.mrfcmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mgb.mrfcmanager.data.local.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Meetings (Agendas)
 * Provides cached meeting data for offline viewing
 */
@Dao
interface MeetingDao {

    // ==================== QUERIES ====================

    /**
     * Get all cached meetings ordered by date
     */
    @Query("SELECT * FROM meetings ORDER BY meeting_date DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    /**
     * Get meetings for a specific quarter
     */
    @Query("SELECT * FROM meetings WHERE quarter_id = :quarterId ORDER BY meeting_date DESC")
    fun getMeetingsByQuarter(quarterId: Long): Flow<List<MeetingEntity>>

    /**
     * Get meetings for a specific MRFC
     */
    @Query("SELECT * FROM meetings WHERE mrfc_id = :mrfcId ORDER BY meeting_date DESC")
    fun getMeetingsByMrfc(mrfcId: Long): Flow<List<MeetingEntity>>

    /**
     * Get a single meeting by ID
     */
    @Query("SELECT * FROM meetings WHERE id = :meetingId")
    suspend fun getMeetingById(meetingId: Long): MeetingEntity?

    /**
     * Get meetings cached within the last N hours
     */
    @Query("""
        SELECT * FROM meetings
        WHERE cached_at > :minTimestamp
        ORDER BY meeting_date DESC
    """)
    fun getRecentlyCachedMeetings(minTimestamp: Long): Flow<List<MeetingEntity>>

    /**
     * Search meetings by title or location
     */
    @Query("""
        SELECT * FROM meetings
        WHERE meeting_title LIKE '%' || :query || '%'
        OR location LIKE '%' || :query || '%'
        OR mrfc_name LIKE '%' || :query || '%'
        ORDER BY meeting_date DESC
    """)
    fun searchMeetings(query: String): Flow<List<MeetingEntity>>

    // ==================== INSERT/UPDATE ====================

    /**
     * Insert a single meeting
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meeting: MeetingEntity)

    /**
     * Insert multiple meetings
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meetings: List<MeetingEntity>)

    // ==================== DELETE ====================

    /**
     * Delete a meeting by ID
     */
    @Query("DELETE FROM meetings WHERE id = :meetingId")
    suspend fun deleteById(meetingId: Long)

    /**
     * Clear all cached meetings
     */
    @Query("DELETE FROM meetings")
    suspend fun clearAll()

    /**
     * Clear stale cache (older than N hours)
     */
    @Query("DELETE FROM meetings WHERE cached_at < :maxAge")
    suspend fun clearStaleCache(maxAge: Long)
}
