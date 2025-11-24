package com.mgb.mrfcmanager.ui.meeting.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.VoiceRecordingApiService
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingDto
import com.mgb.mrfcmanager.data.repository.VoiceRecordingRepository
import com.mgb.mrfcmanager.utils.AudioRecorderHelper
import com.mgb.mrfcmanager.viewmodel.*

/**
 * Fragment for voice recording functionality
 * Allows users to record, save, and play voice recordings for meetings
 */
class VoiceRecordingFragment : Fragment() {

    private lateinit var viewModel: VoiceRecordingViewModel
    private lateinit var adapter: VoiceRecordingAdapter
    private lateinit var audioRecorder: AudioRecorderHelper

    // Views
    private lateinit var tvRecordingTimer: TextView
    private lateinit var tvRecordingStatus: TextView
    private lateinit var btnRecord: MaterialButton
    private lateinit var layoutRecordingActions: View
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var rvRecordings: RecyclerView

    // Arguments
    private var agendaId: Long = 0
    private var mrfcId: Long = 0

    // Timer handler
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (audioRecorder.isRecording()) {
                updateTimerDisplay()
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            Toast.makeText(
                requireContext(),
                "Microphone permission is required for recording",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"
        private const val ARG_MRFC_ID = "mrfc_id"

        fun newInstance(agendaId: Long, mrfcId: Long): VoiceRecordingFragment {
            return VoiceRecordingFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_AGENDA_ID, agendaId)
                    putLong(ARG_MRFC_ID, mrfcId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            agendaId = it.getLong(ARG_AGENDA_ID, 0)
            mrfcId = it.getLong(ARG_MRFC_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_voice_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeStates()

        // Initialize audio recorder
        audioRecorder = AudioRecorderHelper(requireContext())

        // Load recordings
        if (agendaId > 0) {
            viewModel.loadVoiceRecordings(agendaId)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh recordings when returning to this fragment
        if (agendaId > 0) {
            viewModel.loadVoiceRecordings(agendaId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        audioRecorder.release()
        adapter.releasePlayer()
    }

    private fun initializeViews(view: View) {
        tvRecordingTimer = view.findViewById(R.id.tvRecordingTimer)
        tvRecordingStatus = view.findViewById(R.id.tvRecordingStatus)
        btnRecord = view.findViewById(R.id.btnRecord)
        layoutRecordingActions = view.findViewById(R.id.layoutRecordingActions)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSave = view.findViewById(R.id.btnSave)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        rvRecordings = view.findViewById(R.id.rvRecordings)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(VoiceRecordingApiService::class.java)
        val repository = VoiceRecordingRepository(apiService)

        val factory = VoiceRecordingViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[VoiceRecordingViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = VoiceRecordingAdapter { recording ->
            showDeleteConfirmation(recording)
        }
        rvRecordings.layoutManager = LinearLayoutManager(requireContext())
        rvRecordings.adapter = adapter
    }

    private fun setupListeners() {
        btnRecord.setOnClickListener {
            if (audioRecorder.isRecording()) {
                stopRecording()
            } else {
                checkPermissionAndRecord()
            }
        }

        btnCancel.setOnClickListener {
            cancelRecording()
        }

        btnSave.setOnClickListener {
            showSaveDialog()
        }
    }

    private fun observeStates() {
        viewModel.recordingsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VoiceRecordingsState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvEmptyState.visibility = View.GONE
                }
                is VoiceRecordingsState.Success -> {
                    progressBar.visibility = View.GONE
                    if (state.recordings.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        rvRecordings.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.GONE
                        rvRecordings.visibility = View.VISIBLE
                        adapter.submitList(state.recordings)
                    }
                }
                is VoiceRecordingsState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VoiceRecordingUploadState.Uploading -> {
                    progressBar.visibility = View.VISIBLE
                    btnSave.isEnabled = false
                }
                is VoiceRecordingUploadState.Success -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Recording saved successfully", Toast.LENGTH_SHORT).show()
                    resetRecordingUI()
                    viewModel.resetUploadState()
                }
                is VoiceRecordingUploadState.Error -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Upload failed: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.resetUploadState()
                }
                else -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VoiceRecordingDeleteState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteState()
                }
                is VoiceRecordingDeleteState.Error -> {
                    Toast.makeText(requireContext(), "Delete failed: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.resetDeleteState()
                }
                else -> { /* Ignore */ }
            }
        }
    }

    private fun checkPermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Microphone Permission Required")
                    .setMessage("This app needs access to your microphone to record voice notes for meetings.")
                    .setPositiveButton("Grant") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startRecording() {
        if (audioRecorder.startRecording()) {
            // Update UI for recording state
            btnRecord.setIconResource(R.drawable.ic_stop)
            tvRecordingStatus.text = "Recording..."
            tvRecordingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            layoutRecordingActions.visibility = View.GONE

            // Start timer
            timerHandler.post(timerRunnable)
        } else {
            Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        timerHandler.removeCallbacks(timerRunnable)
        val file = audioRecorder.stopRecording()

        if (file != null && file.exists()) {
            // Update UI for stopped state
            btnRecord.setIconResource(R.drawable.ic_mic)
            tvRecordingStatus.text = "Recording complete. Save or cancel."
            tvRecordingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            layoutRecordingActions.visibility = View.VISIBLE
        } else {
            Toast.makeText(requireContext(), "Recording failed", Toast.LENGTH_SHORT).show()
            resetRecordingUI()
        }
    }

    private fun cancelRecording() {
        timerHandler.removeCallbacks(timerRunnable)
        audioRecorder.cancelRecording()
        resetRecordingUI()
        Toast.makeText(requireContext(), "Recording cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun resetRecordingUI() {
        tvRecordingTimer.text = "0:00"
        tvRecordingStatus.text = "Tap to start recording"
        tvRecordingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        btnRecord.setIconResource(R.drawable.ic_mic)
        layoutRecordingActions.visibility = View.GONE
    }

    private fun updateTimerDisplay() {
        val seconds = audioRecorder.getDurationSeconds()
        val minutes = seconds / 60
        val secs = seconds % 60
        tvRecordingTimer.text = String.format("%d:%02d", minutes, secs)
    }

    private fun showSaveDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_save_recording, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etRecordingTitle)
        val tilTitle = dialogView.findViewById<TextInputLayout>(R.id.tilRecordingTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val tvDuration = dialogView.findViewById<TextView>(R.id.tvDialogDuration)

        // Set duration
        val seconds = audioRecorder.getDurationSeconds()
        val minutes = seconds / 60
        val secs = seconds % 60
        tvDuration.text = String.format("%d:%02d", minutes, secs)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Recording")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isEmpty()) {
                    tilTitle.error = "Title is required"
                    return@setPositiveButton
                }

                val audioFile = audioRecorder.getOutputFile()
                if (audioFile != null && audioFile.exists()) {
                    viewModel.uploadVoiceRecording(
                        audioFile = audioFile,
                        agendaId = agendaId,
                        recordingName = title,
                        description = description.ifEmpty { null },
                        durationSeconds = audioRecorder.getDurationSeconds()
                    )
                } else {
                    Toast.makeText(requireContext(), "Recording file not found", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(recording: VoiceRecordingDto) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Recording")
            .setMessage("Are you sure you want to delete \"${recording.recordingName}\"?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteVoiceRecording(recording.id, agendaId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
