package com.mgb.mrfcmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mgb.mrfcmanager.data.local.dao.AgendaItemDao
import com.mgb.mrfcmanager.data.local.dao.CachedFileDao
import com.mgb.mrfcmanager.data.local.dao.MeetingDao
import com.mgb.mrfcmanager.data.local.dao.NoteDao
import com.mgb.mrfcmanager.data.local.dao.PendingSyncDao
import com.mgb.mrfcmanager.data.local.entity.AgendaItemEntity
import com.mgb.mrfcmanager.data.local.entity.CachedFileEntity
import com.mgb.mrfcmanager.data.local.entity.MeetingEntity
import com.mgb.mrfcmanager.data.local.entity.NoteEntity
import com.mgb.mrfcmanager.data.local.entity.PendingSyncEntity

/**
 * MRFC Manager Room Database
 * Provides local storage for offline support
 */
@Database(
    entities = [
        NoteEntity::class,
        MeetingEntity::class,
        AgendaItemEntity::class,
        CachedFileEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MRFCDatabase : RoomDatabase() {

    // DAOs
    abstract fun noteDao(): NoteDao
    abstract fun meetingDao(): MeetingDao
    abstract fun agendaItemDao(): AgendaItemDao
    abstract fun cachedFileDao(): CachedFileDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        private const val DATABASE_NAME = "mrfc_manager_db"

        @Volatile
        private var INSTANCE: MRFCDatabase? = null

        /**
         * Get database instance (singleton)
         */
        fun getInstance(context: Context): MRFCDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): MRFCDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MRFCDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For development; use migrations in production
                .build()
        }

        /**
         * Clear the database instance (for testing)
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
