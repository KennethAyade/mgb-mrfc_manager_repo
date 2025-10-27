package com.mgb.mrfcmanager.ui.admin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Proponent
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AttendanceApiService
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import com.mgb.mrfcmanager.data.repository.AttendanceRepository
import com.mgb.mrfcmanager.utils.DemoData
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.AttendanceListState
import com.mgb.mrfcmanager.viewmodel.AttendanceViewModel
import com.mgb.mrfcmanager.viewmodel.AttendanceViewModelFactory
import com.mgb.mrfcmanager.viewmodel.PhotoUploadState
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var ivAttendancePhoto: ImageView
    private lateinit var btnCapturePhoto: MaterialButton
    private lateinit var cbSelectAll: MaterialCheckBox
    private lateinit var rvAttendance: RecyclerView
    private lateinit var tvPresentCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var btnSaveAttendance: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var attendanceAdapter: AttendanceAdapter
    private val attendanceList = mutableListOf<AttendanceItem>()
    private var attendancePhoto: Bitmap? = null
    private var photoFile: File? = null

    private lateinit var viewModel: AttendanceViewModel
    private var agendaId: Long = 0L
    private var userId: Long = 0L

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
    }

    data class AttendanceItem(
        val proponent: Proponent,
        var isPresent: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        // Get intent extras
        agendaId = intent.getLongExtra("AGENDA_ID", 0L)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModelStates()
        loadAttendanceData()
        updateDateTime()
        updateCounts()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        ivAttendancePhoto = findViewById(R.id.ivAttendancePhoto)
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        rvAttendance = findViewById(R.id.rvAttendance)
        tvPresentCount = findViewById(R.id.tvPresentCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val attendanceApiService = retrofit.create(AttendanceApiService::class.java)
        val attendanceRepository = AttendanceRepository(attendanceApiService)
        val factory = AttendanceViewModelFactory(attendanceRepository)
        viewModel = ViewModelProvider(this, factory)[AttendanceViewModel::class.java]

        // Get userId from TokenManager
        userId = tokenManager.getUserId() ?: 0L
    }

    private fun observeViewModelStates() {
        // Observe photo upload state
        viewModel.photoUploadState.observe(this) { state ->
            when (state) {
                is PhotoUploadState.Uploading -> {
                    showLoading(true)
                }
                is PhotoUploadState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetPhotoUploadState()
                }
                is PhotoUploadState.Error -> {
                    showLoading(false)
                    showError("Photo upload failed: ${state.message}")
                    viewModel.resetPhotoUploadState()
                }
                is PhotoUploadState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSaveAttendance.isEnabled = !isLoading
        btnCapturePhoto.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(attendanceList) { position, isChecked ->
            attendanceList[position].isPresent = isChecked
            updateCounts()
            updateSelectAllCheckbox()
        }
        rvAttendance.layoutManager = LinearLayoutManager(this)
        rvAttendance.adapter = attendanceAdapter
    }

    private fun setupClickListeners() {
        btnCapturePhoto.setOnClickListener {
            capturePhoto()
        }

        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            selectAll(isChecked)
        }

        btnSaveAttendance.setOnClickListener {
            saveAttendance()
        }
    }

    private fun updateDateTime() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentDate = Date()

        tvDate.text = dateFormat.format(currentDate)
        tvTime.text = timeFormat.format(currentDate)
    }

    private fun capturePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                attendancePhoto = imageBitmap
                ivAttendancePhoto.setImageBitmap(imageBitmap)
                ivAttendancePhoto.scaleType = ImageView.ScaleType.CENTER_CROP

                // Save bitmap to file
                photoFile = saveBitmapToFile(imageBitmap)

                Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "attendance_$timestamp.jpg"
        val file = File(cacheDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return file
    }

    private fun loadAttendanceData() {
        // Load proponents from demo data
        DemoData.proponentList.forEach { proponent ->
            attendanceList.add(AttendanceItem(proponent, false))
        }
        attendanceAdapter.notifyDataSetChanged()
    }

    private fun selectAll(isChecked: Boolean) {
        attendanceList.forEach { it.isPresent = isChecked }
        attendanceAdapter.notifyDataSetChanged()
        updateCounts()
    }

    private fun updateSelectAllCheckbox() {
        val allChecked = attendanceList.all { it.isPresent }
        val noneChecked = attendanceList.none { it.isPresent }

        cbSelectAll.setOnCheckedChangeListener(null)
        cbSelectAll.isChecked = allChecked
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            selectAll(isChecked)
        }
    }

    private fun updateCounts() {
        val presentCount = attendanceList.count { it.isPresent }
        val absentCount = attendanceList.size - presentCount

        tvPresentCount.text = presentCount.toString()
        tvAbsentCount.text = absentCount.toString()
    }

    private fun saveAttendance() {
        val presentCount = attendanceList.count { it.isPresent }

        if (agendaId == 0L) {
            showError("Agenda ID is required")
            return
        }

        if (attendancePhoto == null || photoFile == null) {
            Toast.makeText(this, "Please capture attendance photo", Toast.LENGTH_SHORT).show()
            return
        }

        if (presentCount == 0) {
            Toast.makeText(this, "Please mark at least one attendee", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                timeFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val currentTime = timeFormat.format(Date())

                // Create attendance record for current user (admin marking attendance)
                val attendanceRequest = CreateAttendanceRequest(
                    agendaId = agendaId,
                    status = "PRESENT",
                    timeIn = currentTime,
                    remarks = "Attendance marked: $presentCount present, ${attendanceList.size - presentCount} absent"
                )

                when (val result = viewModel.createAttendance(attendanceRequest)) {
                    is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                        val attendanceId = result.data.id

                        // Upload photo
                        photoFile?.let { file ->
                            viewModel.uploadPhoto(attendanceId, file)

                            // Wait for photo upload to complete
                            // The observer will handle success/error and call finish()
                            lifecycleScope.launch {
                                kotlinx.coroutines.delay(2000) // Give time for upload
                                showLoading(false)
                                Toast.makeText(
                                    this@AttendanceActivity,
                                    "Attendance saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                    is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                        showLoading(false)
                        showError("Failed to save attendance: ${result.message}")
                    }
                    else -> {
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Error saving attendance: ${e.localizedMessage}")
            }
        }
    }

    // Adapter for attendance list
    class AttendanceAdapter(
        private val attendanceList: List<AttendanceItem>,
        private val onCheckChanged: (Int, Boolean) -> Unit
    ) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_attendance, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(attendanceList[position], position, onCheckChanged)
        }

        override fun getItemCount() = attendanceList.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cbAttendance: MaterialCheckBox = itemView.findViewById(R.id.cbAttendance)
            private val tvProponentName: TextView = itemView.findViewById(R.id.tvProponentName)
            private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
            private val viewStatus: View = itemView.findViewById(R.id.viewStatus)

            fun bind(
                attendanceItem: AttendanceItem,
                position: Int,
                onCheckChanged: (Int, Boolean) -> Unit
            ) {
                val proponent = attendanceItem.proponent

                tvProponentName.text = proponent.name
                tvCompanyName.text = proponent.companyName

                // Set checkbox without triggering listener
                cbAttendance.setOnCheckedChangeListener(null)
                cbAttendance.isChecked = attendanceItem.isPresent
                cbAttendance.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChanged(position, isChecked)
                }

                // Update status indicator
                val statusColor = if (attendanceItem.isPresent) {
                    R.color.status_success
                } else {
                    R.color.status_pending
                }
                viewStatus.setBackgroundResource(R.drawable.bg_circle)
                viewStatus.backgroundTintList = itemView.context.getColorStateList(statusColor)

                // Make entire item clickable
                itemView.setOnClickListener {
                    cbAttendance.isChecked = !cbAttendance.isChecked
                }
            }
        }
    }
}
