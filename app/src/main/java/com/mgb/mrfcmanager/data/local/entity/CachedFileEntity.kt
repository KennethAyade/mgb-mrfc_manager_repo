package com.mgb.mrfcmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local database entity for tracking cached/downloaded files
 * Used for offline file viewing
 */
@Entity(
    tableName = "cached_files",
    indices = [
        Index(value = ["document_id"]),
        Index(value = ["file_url"], unique = true)
    ]
)
data class CachedFileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "document_id")
    val documentId: Long,

    @ColumnInfo(name = "file_url")
    val fileUrl: String,

    @ColumnInfo(name = "local_file_path")
    val localFilePath: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "original_name")
    val originalName: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "file_type")
    val fileType: String?,

    @ColumnInfo(name = "category")
    val category: String,

    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_fully_downloaded")
    val isFullyDownloaded: Boolean = true
)
