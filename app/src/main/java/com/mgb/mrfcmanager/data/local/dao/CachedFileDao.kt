package com.mgb.mrfcmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mgb.mrfcmanager.data.local.entity.CachedFileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Cached Files
 * Tracks downloaded files for offline viewing
 */
@Dao
interface CachedFileDao {

    // ==================== QUERIES ====================

    /**
     * Get all cached files
     */
    @Query("SELECT * FROM cached_files ORDER BY last_accessed_at DESC")
    fun getAllCachedFiles(): Flow<List<CachedFileEntity>>

    /**
     * Get cached file by document ID
     */
    @Query("SELECT * FROM cached_files WHERE document_id = :documentId LIMIT 1")
    suspend fun getCachedFileByDocumentId(documentId: Long): CachedFileEntity?

    /**
     * Get cached file by URL
     */
    @Query("SELECT * FROM cached_files WHERE file_url = :fileUrl LIMIT 1")
    suspend fun getCachedFileByUrl(fileUrl: String): CachedFileEntity?

    /**
     * Get cached files by category
     */
    @Query("SELECT * FROM cached_files WHERE category = :category ORDER BY last_accessed_at DESC")
    fun getCachedFilesByCategory(category: String): Flow<List<CachedFileEntity>>

    /**
     * Check if file is cached
     */
    @Query("SELECT COUNT(*) > 0 FROM cached_files WHERE document_id = :documentId AND is_fully_downloaded = 1")
    suspend fun isFileCached(documentId: Long): Boolean

    /**
     * Get total cache size
     */
    @Query("SELECT COALESCE(SUM(file_size), 0) FROM cached_files")
    suspend fun getTotalCacheSize(): Long

    /**
     * Get cached files ordered by least recently used (for cleanup)
     */
    @Query("SELECT * FROM cached_files ORDER BY last_accessed_at ASC")
    suspend fun getCachedFilesLRU(): List<CachedFileEntity>

    // ==================== INSERT/UPDATE ====================

    /**
     * Insert or update a cached file record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cachedFile: CachedFileEntity)

    /**
     * Update last accessed timestamp
     */
    @Query("UPDATE cached_files SET last_accessed_at = :timestamp WHERE id = :id")
    suspend fun updateLastAccessed(id: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Mark file as fully downloaded
     */
    @Query("UPDATE cached_files SET is_fully_downloaded = 1 WHERE id = :id")
    suspend fun markAsFullyDownloaded(id: Long)

    // ==================== DELETE ====================

    /**
     * Delete cached file by ID
     */
    @Query("DELETE FROM cached_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Delete cached file by document ID
     */
    @Query("DELETE FROM cached_files WHERE document_id = :documentId")
    suspend fun deleteByDocumentId(documentId: Long)

    /**
     * Delete cached file by local path
     */
    @Query("DELETE FROM cached_files WHERE local_file_path = :localPath")
    suspend fun deleteByLocalPath(localPath: String)

    /**
     * Clear all cached files
     */
    @Query("DELETE FROM cached_files")
    suspend fun clearAll()

    /**
     * Clear files older than specified timestamp
     */
    @Query("DELETE FROM cached_files WHERE last_accessed_at < :maxAge")
    suspend fun clearOldFiles(maxAge: Long)
}
