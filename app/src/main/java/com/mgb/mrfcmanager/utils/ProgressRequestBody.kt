package com.mgb.mrfcmanager.utils

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

/**
 * Custom RequestBody that reports upload progress
 */
class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val listener: UploadProgressListener
) : RequestBody() {

    interface UploadProgressListener {
        fun onProgressUpdate(percentage: Int, bytesUploaded: Long, totalBytes: Long)
    }

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream = FileInputStream(file)
        var uploaded: Long = 0
        var lastProgressUpdate = 0

        try {
            inputStream.use { input ->
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    uploaded += read
                    sink.write(buffer, 0, read)
                    
                    // Flush to ensure data is actually sent
                    sink.flush()
                    
                    // Calculate progress percentage
                    val progress = (100 * uploaded / fileLength).toInt()
                    
                    // Only update if progress changed by at least 1% (throttling)
                    // This prevents overwhelming the UI with too many updates
                    if (progress > lastProgressUpdate) {
                        lastProgressUpdate = progress
                        listener.onProgressUpdate(progress, uploaded, fileLength)
                    }
                }
                
                // Ensure we report 100% at the end
                if (lastProgressUpdate < 100) {
                    listener.onProgressUpdate(100, fileLength, fileLength)
                }
            }
        } finally {
            inputStream.close()
        }
    }

    companion object {
        // Increased buffer size for better performance (64KB)
        // Larger buffer = fewer updates, smoother progress
        private const val DEFAULT_BUFFER_SIZE = 65536 // 64KB
    }
}

