package com.mgb.mrfcmanager.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.PowerManager
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Helper class for audio recording functionality
 * Uses MediaRecorder to record audio in M4A format (AAC codec)
 * Includes WakeLock to prevent CPU sleep during recording (Feature 8 fix)
 */
class AudioRecorderHelper(private val context: Context) {

    companion object {
        private const val TAG = "AudioRecorderHelper"
        private const val WAKE_LOCK_TAG = "MRFCManager:AudioRecorderWakeLock"

        /**
         * Clean up old recording files from cache
         * @param context Application context
         * @param maxAgeMs Maximum age of files to keep (default 24 hours)
         */
        fun cleanupOldRecordings(context: Context, maxAgeMs: Long = 24 * 60 * 60 * 1000) {
            val recordingsDir = File(context.cacheDir, "recordings")
            if (recordingsDir.exists()) {
                val now = System.currentTimeMillis()
                recordingsDir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > maxAgeMs) {
                        file.delete()
                    }
                }
            }
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var startTime: Long = 0

    // WakeLock to prevent CPU sleep during recording (Feature 8)
    private var wakeLock: PowerManager.WakeLock? = null

    // Error listener for MediaRecorder errors
    private var onErrorListener: ((what: Int, extra: Int) -> Unit)? = null

    /**
     * Set an error listener for MediaRecorder errors
     * @param listener Callback with error code (what) and extra info
     */
    fun setOnErrorListener(listener: ((what: Int, extra: Int) -> Unit)?) {
        onErrorListener = listener
    }

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Get the output file path
     */
    fun getOutputFile(): File? = outputFile

    /**
     * Get recording duration in seconds
     */
    fun getDurationSeconds(): Int {
        return if (startTime > 0) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }
    }

    /**
     * Acquire a partial WakeLock to prevent CPU from sleeping during recording
     */
    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    WAKE_LOCK_TAG
                )
            }
            wakeLock?.let { lock ->
                if (!lock.isHeld) {
                    // Acquire with timeout of 2 hours max to prevent battery drain
                    lock.acquire(2 * 60 * 60 * 1000L)
                    Log.d(TAG, "WakeLock acquired for recording")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }

    /**
     * Release the WakeLock when recording stops
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }

    /**
     * Start recording audio
     * @return true if recording started successfully
     */
    fun startRecording(): Boolean {
        try {
            // Acquire WakeLock to prevent CPU sleep (Feature 8)
            acquireWakeLock()

            // Create output file in cache directory
            val cacheDir = context.cacheDir
            val recordingsDir = File(cacheDir, "recordings")
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            outputFile = File(recordingsDir, "recording_$timestamp.m4a")

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile?.absolutePath)

                // Set error listener to handle recording errors (Feature 8)
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaRecorder error: what=$what, extra=$extra")
                    onErrorListener?.invoke(what, extra)
                }

                // Set info listener for max duration/filesize warnings
                setOnInfoListener { _, what, _ ->
                    Log.d(TAG, "MediaRecorder info: what=$what")
                }

                prepare()
                start()
            }

            isRecording = true
            startTime = System.currentTimeMillis()
            Log.d(TAG, "Recording started: ${outputFile?.absolutePath}")
            return true
        } catch (e: IOException) {
            Log.e(TAG, "IOException during startRecording", e)
            releaseWakeLock()
            releaseRecorder()
            return false
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException during startRecording", e)
            releaseWakeLock()
            releaseRecorder()
            return false
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during startRecording", e)
            releaseWakeLock()
            releaseRecorder()
            return false
        }
    }

    /**
     * Stop recording and save the file
     * @return The recorded audio file, or null if recording failed
     */
    fun stopRecording(): File? {
        if (!isRecording) {
            return null
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            Log.d(TAG, "Recording stopped: ${outputFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }

        // Release WakeLock (Feature 8)
        releaseWakeLock()

        mediaRecorder = null
        isRecording = false

        return outputFile
    }

    /**
     * Cancel recording and delete any partial file
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore errors when canceling
            Log.w(TAG, "Error during cancelRecording", e)
        }

        // Release WakeLock (Feature 8)
        releaseWakeLock()

        mediaRecorder = null
        isRecording = false

        // Delete the partial file
        outputFile?.delete()
        outputFile = null
        startTime = 0
        Log.d(TAG, "Recording cancelled")
    }

    /**
     * Release recorder resources
     */
    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null
        isRecording = false
        outputFile = null
        startTime = 0
    }

    /**
     * Clean up resources
     * Call this when the helper is no longer needed
     */
    fun release() {
        if (isRecording) {
            cancelRecording()
        } else {
            releaseRecorder()
        }
        // Ensure WakeLock is released
        releaseWakeLock()
    }
}
