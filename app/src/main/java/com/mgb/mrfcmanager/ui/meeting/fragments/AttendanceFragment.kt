package com.mgb.mrfcmanager.ui.meeting.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.button.MaterialButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AttendanceApiService
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.repository.AttendanceRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.AttendanceListState
import com.mgb.mrfcmanager.viewmodel.AttendanceViewModel
import com.mgb.mrfcmanager.viewmodel.AttendanceViewModelFactory
import com.mgb.mrfcmanager.viewmodel.PhotoUploadState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Attendance Fragment
 * Shows attendance list for the meeting
 * ALL users can log their attendance with photo
 */
class AttendanceFragment : Fragment() {

    private lateinit var rvAttendance: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvSummary: TextView
    private lateinit var btnLogAttendance: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: AttendanceViewModel
    private lateinit var attendanceAdapter: AttendanceListAdapter

    private val attendanceList = mutableListOf<AttendanceDto>()
    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"
        private const val ARG_MRFC_ID = "mrfc_id"
        private const val REQUEST_IMAGE_CAPTURE = 101

        fun newInstance(agendaId: Long, mrfcId: Long): AttendanceFragment {
            return AttendanceFragment().apply {
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
            agendaId = it.getLong(ARG_AGENDA_ID)
            mrfcId = it.getLong(ARG_MRFC_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attendance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadAttendance()

        btnLogAttendance.setOnClickListener {
            logMyAttendance()
        }
    }

    private fun initializeViews(view: View) {
        rvAttendance = view.findViewById(R.id.rvAttendance)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvSummary = view.findViewById(R.id.tvSummary)
        btnLogAttendance = view.findViewById(R.id.btnLogAttendance)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(AttendanceApiService::class.java)
        val repository = AttendanceRepository(apiService)
        val factory = AttendanceViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AttendanceViewModel::class.java]
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceListAdapter(attendanceList) { attendance ->
            showAttendanceDetails(attendance)
        }
        rvAttendance.layoutManager = LinearLayoutManager(requireContext())
        rvAttendance.adapter = attendanceAdapter
    }

    private fun showAttendanceDetails(attendance: AttendanceDto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_attendance_detail, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set data
        dialogView.findViewById<TextView>(R.id.tvDetailName).text =
            attendance.attendeeName ?: "Attendee #${attendance.id}"
        dialogView.findViewById<TextView>(R.id.tvDetailPosition).text =
            attendance.attendeePosition ?: "N/A"
        dialogView.findViewById<TextView>(R.id.tvDetailDepartment).text =
            attendance.attendeeDepartment ?: "N/A"
        dialogView.findViewById<TextView>(R.id.tvDetailTimestamp).text =
            "Logged at: ${attendance.markedAt}"
        dialogView.findViewById<TextView>(R.id.tvDetailStatus).apply {
            text = if (attendance.isPresent) "Present" else "Absent"
            setTextColor(
                if (attendance.isPresent)
                    context.getColor(R.color.status_success)
                else
                    context.getColor(R.color.status_pending)
            )
        }

        // Load photo if available
        val ivPhoto = dialogView.findViewById<ImageView>(R.id.ivDetailPhoto)
        if (attendance.photoUrl != null) {
            // Load photo using Coil
            ivPhoto.load(attendance.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
            }
        } else {
            ivPhoto.setImageResource(R.drawable.ic_person)
        }

        // Close button
        dialogView.findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.attendanceListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AttendanceListState.Loading -> {
                    showLoading(true)
                }
                is AttendanceListState.Success -> {
                    showLoading(false)
                    updateAttendanceList(state.data)
                }
                is AttendanceListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is AttendanceListState.Idle -> {
                    showLoading(false)
                }
            }
        }

        viewModel.attendanceSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                tvSummary.text = "Present: ${it.present} | Absent: ${it.absent} | Rate: ${it.attendanceRate}%"
                tvSummary.visibility = View.VISIBLE
            } ?: run {
                tvSummary.visibility = View.GONE
            }
        }

        viewModel.photoUploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PhotoUploadState.Success -> {
                    Toast.makeText(requireContext(), "Attendance logged successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetPhotoUploadState()
                }
                is PhotoUploadState.Error -> {
                    showError("Failed to log attendance: ${state.message}")
                    viewModel.resetPhotoUploadState()
                }
                else -> {
                    // Other states
                }
            }
        }
    }

    private fun loadAttendance() {
        viewModel.loadAttendanceByMeeting(agendaId)
    }

    private fun updateAttendanceList(attendance: List<AttendanceDto>) {
        attendanceList.clear()
        attendanceList.addAll(attendance)
        attendanceAdapter.notifyDataSetChanged()

        if (attendanceList.isEmpty()) {
            rvAttendance.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvAttendance.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun logMyAttendance() {
        // Open full AttendanceActivity for better UX with photo capture
        val intent = Intent(requireContext(), com.mgb.mrfcmanager.ui.admin.AttendanceActivity::class.java).apply {
            putExtra("AGENDA_ID", agendaId)
        }
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Reload attendance when returning from AttendanceActivity
        loadAttendance()
    }
}

/**
 * Adapter for displaying attendance list
 */
class AttendanceListAdapter(
    private val attendance: List<AttendanceDto>,
    private val onItemClick: (AttendanceDto) -> Unit
) : RecyclerView.Adapter<AttendanceListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_simple, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(attendance[position])
    }

    override fun getItemCount() = attendance.size

    class ViewHolder(
        itemView: View,
        private val onItemClick: (AttendanceDto) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPosition: TextView? = itemView.findViewById(R.id.tvPosition)
        private val tvDepartment: TextView? = itemView.findViewById(R.id.tvDepartment)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvTimestamp: TextView? = itemView.findViewById(R.id.tvTimestamp)

        fun bind(attendance: AttendanceDto) {
            // Set click listener on the whole card
            itemView.setOnClickListener {
                onItemClick(attendance)
            }

            // Display name
            tvName.text = attendance.attendeeName ?: "Attendee #${attendance.id}"

            // Display position if available
            tvPosition?.apply {
                if (attendance.attendeePosition != null) {
                    text = attendance.attendeePosition
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            // Display department if available
            tvDepartment?.apply {
                if (attendance.attendeeDepartment != null) {
                    text = attendance.attendeeDepartment
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            // Display status
            tvStatus.text = if (attendance.isPresent) "Present" else "Absent"
            tvStatus.setTextColor(
                if (attendance.isPresent)
                    itemView.context.getColor(R.color.status_success)
                else
                    itemView.context.getColor(R.color.status_pending)
            )

            // Display timestamp
            tvTimestamp?.apply {
                text = "Logged at: ${attendance.markedAt}"
                visibility = View.VISIBLE
            }
        }
    }
}
