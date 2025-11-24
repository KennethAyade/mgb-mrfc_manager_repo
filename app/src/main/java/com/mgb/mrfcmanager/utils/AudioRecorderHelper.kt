package com.mgb.mrfcmanager.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * Helper class for audio recording functionality
 * Uses MediaRecorder to record audio in M4A format (AAC codec)
 */
class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var startTime: Long = 0

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
     * Start recording audio
     * @return true if recording started successfully
     */
    fun startRecording(): Boolean {
        try {
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

                prepare()
                start()
            }

            isRecording = true
            startTime = System.currentTimeMillis()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            releaseRecorder()
            return false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            releaseRecorder()
            return false
        } catch (e: SecurityException) {
            e.printStackTrace()
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
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
        }

        mediaRecorder = null
        isRecording = false

        // Delete the partial file
        outputFile?.delete()
        outputFile = null
        startTime = 0
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
    }

    companion object {
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
}
