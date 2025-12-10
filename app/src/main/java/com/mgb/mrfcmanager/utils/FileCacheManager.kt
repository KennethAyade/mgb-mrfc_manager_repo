package com.mgb.mrfcmanager.utils

import android.content.Context
import android.util.Log
import com.mgb.mrfcmanager.data.local.dao.CachedFileDao
import com.mgb.mrfcmanager.data.local.entity.CachedFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Manager for caching and retrieving files for offline viewing
 * Implements LRU cache with configurable size limit
 */
class FileCacheManager(
    private val context: Context,
    private val cachedFileDao: CachedFileDao,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "FileCacheManager"
        private const val CACHE_DIR_NAME = "document_cache"
        private const val DEFAULT_MAX_CACHE_SIZE = 500L * 1024 * 1024 // 500 MB default
    }

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    var maxCacheSize: Long = DEFAULT_MAX_CACHE_SIZE

    /**
     * Check if a document is cached locally
     */
    suspend fun isFileCached(documentId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val cached = cachedFileDao.isFileCached(documentId)
            if (cached) {
                // Verify file still exists on disk
                val entity = cachedFileDao.getCachedFileByDocumentId(documentId)
                entity?.let { File(it.localFilePath).exists() } ?: false
            } else {
                false
            }
        }
    }

    /**
     * Get local file path for a cached document
     */
    suspend fun getCachedFilePath(documentId: Long): String? {
        return withContext(Dispatchers.IO) {
            val entity = cachedFileDao.getCachedFileByDocumentId(documentId)
            entity?.let {
                val file = File(it.localFilePath)
                if (file.exists()) {
                    // Update last accessed time
                    cachedFileDao.updateLastAccessed(it.id)
                    it.localFilePath
                } else {
                    // File was deleted, remove from database
                    cachedFileDao.deleteById(it.id)
                    null
                }
            }
        }
    }

    /**
     * Download and cache a file
     * @return Local file path if successful, null otherwise
     */
    suspend fun downloadAndCache(
        documentId: Long,
        fileUrl: String,
        fileName: String,
        originalName: String,
        category: String,
        authToken: String? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if already cached
                val existingCache = cachedFileDao.getCachedFileByDocumentId(documentId)
                if (existingCache != null && File(existingCache.localFilePath).exists()) {
                    cachedFileDao.updateLastAccessed(existingCache.id)
                    return@withContext existingCache.localFilePath
                }

                // Ensure we have space
                ensureCacheSpace()

                // Create local file
                val localFile = File(cacheDir, "${documentId}_${fileName}")

                // Download file
                val requestBuilder = Request.Builder().url(fileUrl)
                authToken?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }

                val response = okHttpClient.newCall(requestBuilder.build()).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed: ${response.code}")
                    return@withContext null
                }

                val body = response.body ?: return@withContext null
                val fileSize = body.contentLength()
                val contentType = body.contentType()?.toString()

                // Write to local file
                FileOutputStream(localFile).use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }

                // Verify download completed
                if (!localFile.exists() || localFile.length() == 0L) {
                    Log.e(TAG, "Download incomplete")
                    return@withContext null
                }

                // Save to database
                val cacheEntity = CachedFileEntity(
                    documentId = documentId,
                    fileUrl = fileUrl,
                    localFilePath = localFile.absolutePath,
                    fileName = fileName,
                    originalName = originalName,
                    fileSize = localFile.length(),
                    fileType = contentType,
                    category = category,
                    isFullyDownloaded = true
                )

                cachedFileDao.insert(cacheEntity)

                Log.d(TAG, "File cached: ${localFile.absolutePath}")
                localFile.absolutePath

            } catch (e: Exception) {
                Log.e(TAG, "Error caching file", e)
                null
            }
        }
    }

    /**
     * Get all cached files as a Flow
     */
    fun getAllCachedFiles(): Flow<List<CachedFileEntity>> {
        return cachedFileDao.getAllCachedFiles()
    }

    /**
     * Get total cache size in bytes
     */
    suspend fun getTotalCacheSize(): Long {
        return withContext(Dispatchers.IO) {
            cachedFileDao.getTotalCacheSize()
        }
    }

    /**
     * Ensure cache directory has space by removing LRU files
     */
    private suspend fun ensureCacheSpace() {
        val currentSize = cachedFileDao.getTotalCacheSize()
        if (currentSize >= maxCacheSize) {
            // Remove oldest files until we're under 80% capacity
            val targetSize = (maxCacheSize * 0.8).toLong()
            var freedSize = 0L

            val lruFiles = cachedFileDao.getCachedFilesLRU()
            for (cached in lruFiles) {
                if (currentSize - freedSize <= targetSize) break

                // Delete file from disk
                val file = File(cached.localFilePath)
                if (file.exists()) {
                    freedSize += file.length()
                    file.delete()
                }

                // Remove from database
                cachedFileDao.deleteById(cached.id)

                Log.d(TAG, "Removed cached file: ${cached.fileName}")
            }

            Log.d(TAG, "Freed ${freedSize / (1024 * 1024)} MB from cache")
        }
    }

    /**
     * Delete a specific cached file
     */
    suspend fun deleteCachedFile(documentId: Long) {
        withContext(Dispatchers.IO) {
            val entity = cachedFileDao.getCachedFileByDocumentId(documentId)
            entity?.let {
                File(it.localFilePath).delete()
                cachedFileDao.deleteByDocumentId(documentId)
            }
        }
    }

    /**
     * Clear all cached files
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            // Delete all files in cache directory
            cacheDir.listFiles()?.forEach { it.delete() }

            // Clear database
            cachedFileDao.clearAll()

            Log.d(TAG, "Cache cleared")
        }
    }

    /**
     * Clear old cached files (not accessed in the last N days)
     */
    suspend fun clearOldCache(maxAgeDays: Int = 30) {
        withContext(Dispatchers.IO) {
            val maxAgeMs = maxAgeDays * 24 * 60 * 60 * 1000L
            val cutoffTime = System.currentTimeMillis() - maxAgeMs

            // Get old files and delete them from disk
            val lruFiles = cachedFileDao.getCachedFilesLRU()
            for (cached in lruFiles) {
                if (cached.lastAccessedAt < cutoffTime) {
                    File(cached.localFilePath).delete()
                }
            }

            // Clear from database
            cachedFileDao.clearOldFiles(cutoffTime)

            Log.d(TAG, "Old cache cleared (files older than $maxAgeDays days)")
        }
    }
}
