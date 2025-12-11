package com.mgb.mrfcmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mgb.mrfcmanager.data.local.entity.AgendaItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Agenda Items
 * Provides cached agenda item data for offline viewing
 */
@Dao
interface AgendaItemDao {

    // ==================== QUERIES ====================

    /**
     * Get all agenda items for a meeting (excluding other matters)
     */
    @Query("""
        SELECT * FROM agenda_items
        WHERE agenda_id = :agendaId AND is_other_matter = 0
        ORDER BY order_index ASC, created_at ASC
    """)
    fun getAgendaItems(agendaId: Long): Flow<List<AgendaItemEntity>>

    /**
     * Get other matters for a meeting
     */
    @Query("""
        SELECT * FROM agenda_items
        WHERE agenda_id = :agendaId AND is_other_matter = 1
        ORDER BY order_index ASC, created_at ASC
    """)
    fun getOtherMatters(agendaId: Long): Flow<List<AgendaItemEntity>>

    /**
     * Get highlighted items for a meeting
     */
    @Query("""
        SELECT * FROM agenda_items
        WHERE agenda_id = :agendaId AND is_highlighted = 1
        ORDER BY order_index ASC
    """)
    fun getHighlightedItems(agendaId: Long): Flow<List<AgendaItemEntity>>

    /**
     * Get a single agenda item by ID
     */
    @Query("SELECT * FROM agenda_items WHERE id = :itemId")
    suspend fun getAgendaItemById(itemId: Long): AgendaItemEntity?

    /**
     * Get all agenda items for a meeting (including other matters)
     */
    @Query("SELECT * FROM agenda_items WHERE agenda_id = :agendaId ORDER BY order_index ASC")
    fun getAllItemsForMeeting(agendaId: Long): Flow<List<AgendaItemEntity>>

    // ==================== INSERT/UPDATE ====================

    /**
     * Insert a single agenda item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AgendaItemEntity)

    /**
     * Insert multiple agenda items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgendaItemEntity>)

    /**
     * Update highlight status
     */
    @Query("""
        UPDATE agenda_items
        SET is_highlighted = :isHighlighted,
            highlighted_by = :highlightedBy,
            highlighted_at = :highlightedAt,
            cached_at = :cachedAt
        WHERE id = :itemId
    """)
    suspend fun updateHighlightStatus(
        itemId: Long,
        isHighlighted: Boolean,
        highlightedBy: Long?,
        highlightedAt: String?,
        cachedAt: Long = System.currentTimeMillis()
    )

    // ==================== DELETE ====================

    /**
     * Delete agenda item by ID
     */
    @Query("DELETE FROM agenda_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    /**
     * Delete all items for a meeting
     */
    @Query("DELETE FROM agenda_items WHERE agenda_id = :agendaId")
    suspend fun deleteByAgendaId(agendaId: Long)

    /**
     * Clear all cached agenda items
     */
    @Query("DELETE FROM agenda_items")
    suspend fun clearAll()
}
