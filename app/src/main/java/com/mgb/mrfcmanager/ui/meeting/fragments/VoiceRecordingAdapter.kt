package com.mgb.mrfcmanager.ui.meeting.fragments

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.ApiConfig
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingDto

/**
 * Adapter for displaying voice recordings in a RecyclerView
 */
class VoiceRecordingAdapter(
    private val context: Context,
    private val onDeleteClick: (VoiceRecordingDto) -> Unit
) : ListAdapter<VoiceRecordingDto, VoiceRecordingAdapter.ViewHolder>(DiffCallback()) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingId: Long? = null
    private var currentlyPlayingHolder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voice_recording, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recording = getItem(position)
        holder.bind(recording)
    }

    /**
     * Release media player resources
     */
    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentlyPlayingId = null
        currentlyPlayingHolder?.updatePlayButton(false)
        currentlyPlayingHolder = null
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnPlayPause: MaterialButton = itemView.findViewById(R.id.btnPlayPause)
        private val tvRecordingName: TextView = itemView.findViewById(R.id.tvRecordingName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        private val tvRecorder: TextView = itemView.findViewById(R.id.tvRecorder)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(recording: VoiceRecordingDto) {
            tvRecordingName.text = recording.recordingName
            tvDuration.text = recording.formattedDuration
            tvFileSize.text = recording.formattedFileSize
            tvRecorder.text = recording.recorderName

            // Show description if present
            if (!recording.description.isNullOrBlank()) {
                tvDescription.text = recording.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // Update play button state
            val isPlaying = currentlyPlayingId == recording.id
            updatePlayButton(isPlaying)

            // Play/Pause button click
            btnPlayPause.setOnClickListener {
                if (currentlyPlayingId == recording.id) {
                    // Stop current playback
                    stopPlayback()
                } else {
                    // Start playback
                    playRecording(recording, this)
                }
            }

            // Delete button click
            btnDelete.setOnClickListener {
                onDeleteClick(recording)
            }
        }

        fun updatePlayButton(isPlaying: Boolean) {
            btnPlayPause.setIconResource(
                if (isPlaying) R.drawable.ic_stop else R.drawable.ic_play
            )
        }
    }

    private fun playRecording(recording: VoiceRecordingDto, holder: ViewHolder) {
        // Stop any current playback
        stopPlayback()

        try {
            // Get auth token using TokenManager
            val tokenManager = MRFCManagerApp.getTokenManager()
            val token = tokenManager.getAccessToken()
            if (token == null) {
                Toast.makeText(context, "Authentication required. Please log in again.", Toast.LENGTH_LONG).show()
                return
            }

            // Construct backend streaming URL with token as query parameter
            // This bypasses S3 access restrictions by streaming through the backend
            val baseUrl = ApiConfig.BASE_URL.removeSuffix("/")
            val streamingUrl = "$baseUrl/voice-recordings/${recording.id}/stream?token=$token"

            Log.d("VoiceRecordingAdapter", "Streaming from backend: $streamingUrl")

            // Show loading indication
            Toast.makeText(context, "Loading recording...", Toast.LENGTH_SHORT).show()

            mediaPlayer = MediaPlayer().apply {
                // Set audio attributes for better compatibility
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(streamingUrl)
                prepareAsync()
                setOnPreparedListener {
                    Log.d("VoiceRecordingAdapter", "MediaPlayer prepared, starting playback")
                    start()
                    currentlyPlayingId = recording.id
                    currentlyPlayingHolder = holder
                    holder.updatePlayButton(true)
                }
                setOnCompletionListener {
                    Log.d("VoiceRecordingAdapter", "Playback completed")
                    stopPlayback()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("VoiceRecordingAdapter", "MediaPlayer error: what=$what, extra=$extra")
                    val errorMessage = when {
                        what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == -2147483648 ->
                            "Network error - check your connection"
                        what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == -1004 ->
                            "File not found on server"
                        what == MediaPlayer.MEDIA_ERROR_UNKNOWN ->
                            "Unable to load audio. Try again later."
                        what == MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                            "Server error. Please try again."
                        what == MediaPlayer.MEDIA_ERROR_IO ->
                            "Network error - check your connection"
                        what == MediaPlayer.MEDIA_ERROR_MALFORMED ->
                            "Audio format not supported"
                        what == MediaPlayer.MEDIA_ERROR_UNSUPPORTED ->
                            "Audio format not supported"
                        what == MediaPlayer.MEDIA_ERROR_TIMED_OUT ->
                            "Request timed out. Check your connection."
                        else -> "Playback error (code: $what)"
                    }
                    Toast.makeText(
                        context,
                        "Failed to play: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                    stopPlayback()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceRecordingAdapter", "Exception playing recording", e)
            val message = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error - check your connection"
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Request timed out"
                else -> "Failed to play recording"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            stopPlayback()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentlyPlayingHolder?.updatePlayButton(false)
        currentlyPlayingId = null
        currentlyPlayingHolder = null
    }

    class DiffCallback : DiffUtil.ItemCallback<VoiceRecordingDto>() {
        override fun areItemsTheSame(oldItem: VoiceRecordingDto, newItem: VoiceRecordingDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VoiceRecordingDto, newItem: VoiceRecordingDto): Boolean {
            return oldItem == newItem
        }
    }
}
