package com.mgb.mrfcmanager.data.local.database

import androidx.room.TypeConverter
import com.mgb.mrfcmanager.data.local.entity.SyncEntityType
import com.mgb.mrfcmanager.data.local.entity.SyncOperation
import com.mgb.mrfcmanager.data.local.entity.SyncStatus

/**
 * Room type converters for enum types
 */
class Converters {

    // ==================== SyncStatus ====================

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    // ==================== SyncOperation ====================

    @TypeConverter
    fun fromSyncOperation(operation: SyncOperation): String = operation.name

    @TypeConverter
    fun toSyncOperation(value: String): SyncOperation = SyncOperation.valueOf(value)

    // ==================== SyncEntityType ====================

    @TypeConverter
    fun fromSyncEntityType(type: SyncEntityType): String = type.name

    @TypeConverter
    fun toSyncEntityType(value: String): SyncEntityType = SyncEntityType.valueOf(value)
}
