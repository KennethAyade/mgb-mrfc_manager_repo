package com.mgb.mrfcmanager.ui.meeting.fragments

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingDto

/**
 * Adapter for displaying voice recordings in a RecyclerView
 */
class VoiceRecordingAdapter(
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
            mediaPlayer = MediaPlayer().apply {
                setDataSource(recording.fileUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    currentlyPlayingId = recording.id
                    currentlyPlayingHolder = holder
                    holder.updatePlayButton(true)
                }
                setOnCompletionListener {
                    stopPlayback()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlayback()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
