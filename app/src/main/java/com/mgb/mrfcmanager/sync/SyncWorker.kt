package com.mgb.mrfcmanager.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.data.local.dao.NoteDao
import com.mgb.mrfcmanager.data.local.dao.PendingSyncDao
import com.mgb.mrfcmanager.data.local.database.MRFCDatabase
import com.mgb.mrfcmanager.data.local.entity.NoteEntity
import com.mgb.mrfcmanager.data.local.entity.SyncEntityType
import com.mgb.mrfcmanager.data.local.entity.SyncStatus
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.NotesApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import java.util.concurrent.TimeUnit

/**
 * Background worker for syncing pending offline changes with the server
 * Uses WorkManager to handle background processing and retries
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncWorker"
        private const val UNIQUE_WORK_NAME = "MRFC_SYNC_WORK"
        private const val PERIODIC_WORK_NAME = "MRFC_PERIODIC_SYNC"
        private const val SYNC_INTERVAL_HOURS = 1L

        /**
         * Schedule an immediate sync when network becomes available
         */
        fun scheduleImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

            Log.d(TAG, "Immediate sync scheduled")
        }

        /**
         * Schedule periodic sync (hourly when connected)
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest
                )

            Log.d(TAG, "Periodic sync scheduled (every $SYNC_INTERVAL_HOURS hours)")
        }

        /**
         * Cancel all scheduled sync work
         */
        fun cancelAllSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            Log.d(TAG, "All sync work cancelled")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work...")

        return try {
            val database = MRFCDatabase.getInstance(applicationContext)
            val noteDao = database.noteDao()
            val pendingSyncDao = database.pendingSyncDao()

            val tokenManager = MRFCManagerApp.getTokenManager()
            val retrofit = RetrofitClient.getInstance(tokenManager)
            val notesApiService = retrofit.create(NotesApiService::class.java)

            // Sync pending notes
            val syncedNotes = syncPendingNotes(noteDao, pendingSyncDao, notesApiService)

            Log.d(TAG, "Sync completed. Notes synced: $syncedNotes")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Sync all pending note changes
     */
    private suspend fun syncPendingNotes(
        noteDao: NoteDao,
        pendingSyncDao: PendingSyncDao,
        apiService: NotesApiService
    ): Int {
        val pendingNotes = noteDao.getPendingSyncNotes()
        var syncedCount = 0

        for (note in pendingNotes) {
            try {
                val success = when (note.syncStatus) {
                    SyncStatus.PENDING_CREATE -> syncCreateNote(note, noteDao, pendingSyncDao, apiService)
                    SyncStatus.PENDING_UPDATE -> syncUpdateNote(note, noteDao, pendingSyncDao, apiService)
                    SyncStatus.PENDING_DELETE -> syncDeleteNote(note, noteDao, pendingSyncDao, apiService)
                    SyncStatus.SYNCED -> true
                }

                if (success) syncedCount++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync note ${note.id}", e)
            }
        }

        return syncedCount
    }

    private suspend fun syncCreateNote(
        note: NoteEntity,
        noteDao: NoteDao,
        pendingSyncDao: PendingSyncDao,
        apiService: NotesApiService
    ): Boolean {
        val request = CreateNoteRequest(
            mrfcId = note.mrfcId,
            quarterId = note.quarterId,
            agendaId = note.agendaId,
            title = note.title,
            content = note.content ?: "",
            isPinned = note.isPinned
        )

        val response = apiService.createNote(request)

        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data?.let { serverNote ->
                // Delete local note with temporary ID
                noteDao.deleteById(note.id)
                // Insert with server ID
                noteDao.insert(NoteEntity.fromDto(serverNote))
                // Remove from pending sync
                pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, note.id)

                Log.d(TAG, "Note created: local ${note.id} -> server ${serverNote.id}")
                return true
            }
        }

        return false
    }

    private suspend fun syncUpdateNote(
        note: NoteEntity,
        noteDao: NoteDao,
        pendingSyncDao: PendingSyncDao,
        apiService: NotesApiService
    ): Boolean {
        val request = UpdateNoteRequest(
            title = note.title,
            content = note.content,
            isPinned = note.isPinned
        )

        val response = apiService.updateNote(note.id, request)

        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data?.let { serverNote ->
                noteDao.insert(NoteEntity.fromDto(serverNote))
                pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, note.id)

                Log.d(TAG, "Note updated: ${note.id}")
                return true
            }
        }

        return false
    }

    private suspend fun syncDeleteNote(
        note: NoteEntity,
        noteDao: NoteDao,
        pendingSyncDao: PendingSyncDao,
        apiService: NotesApiService
    ): Boolean {
        val response = apiService.deleteNote(note.id)

        if (response.isSuccessful && response.body()?.success == true) {
            noteDao.deleteById(note.id)
            pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, note.id)

            Log.d(TAG, "Note deleted: ${note.id}")
            return true
        }

        return false
    }
}
