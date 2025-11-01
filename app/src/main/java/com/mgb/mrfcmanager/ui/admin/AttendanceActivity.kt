package com.mgb.mrfcmanager.ui.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AttendanceApiService
import com.mgb.mrfcmanager.data.repository.AttendanceRepository
import com.mgb.mrfcmanager.viewmodel.AttendanceViewModel
import com.mgb.mrfcmanager.viewmodel.AttendanceViewModelFactory
import com.mgb.mrfcmanager.viewmodel.PhotoUploadState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Attendance Activity - Onsite Meeting Attendance Logging
 *
 * PURPOSE:
 * A tablet with this app is placed in the meeting room.
 * Attendees use the tablet to log their own attendance by:
 * 1. Entering their Full Name
 * 2. Entering their Position/Designation
 * 3. Entering their Department/Agency/Organization
 * 4. Capturing a live photo for verification
 * 5. Submitting the form
 *
 * After submission, the form clears for the next attendee.
 */
class AttendanceActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var etFullName: EditText
    private lateinit var etPosition: EditText
    private lateinit var etDepartment: EditText
    private lateinit var ivAttendancePhoto: ImageView
    private lateinit var btnCapturePhoto: MaterialButton
    private lateinit var tvPresentCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var btnSubmitAttendance: MaterialButton
    private lateinit var progressBar: ProgressBar

    // State
    private var attendancePhoto: Bitmap? = null
    private var photoFile: File? = null
    private lateinit var viewModel: AttendanceViewModel
    private var agendaId: Long = 0L

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_CAMERA_PERMISSION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        // Get agenda ID from intent
        agendaId = intent.getLongExtra("AGENDA_ID", 0L)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupClickListeners()
        observeViewModelStates()
        updateDateTime()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Log Attendance"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        etFullName = findViewById(R.id.etFullName)
        etPosition = findViewById(R.id.etPosition)
        etDepartment = findViewById(R.id.etDepartment)
        ivAttendancePhoto = findViewById(R.id.ivAttendancePhoto)
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto)
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val attendanceApiService = retrofit.create(AttendanceApiService::class.java)
        val attendanceRepository = AttendanceRepository(attendanceApiService)
        val factory = AttendanceViewModelFactory(attendanceRepository)
        viewModel = ViewModelProvider(this, factory)[AttendanceViewModel::class.java]
    }

    private fun observeViewModelStates() {
        viewModel.photoUploadState.observe(this) { state ->
            when (state) {
                is PhotoUploadState.Uploading -> {
                    showLoading(true)
                }
                is PhotoUploadState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Attendance logged successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetPhotoUploadState()
                    clearForm()
                }
                is PhotoUploadState.Error -> {
                    showLoading(false)
                    showError("Failed to log attendance: ${state.message}")
                    viewModel.resetPhotoUploadState()
                }
                is PhotoUploadState.Idle -> {
                    // No action needed
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSubmitAttendance.isEnabled = !isLoading
        btnCapturePhoto.isEnabled = !isLoading
        etFullName.isEnabled = !isLoading
        etPosition.isEnabled = !isLoading
        etDepartment.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        btnCapturePhoto.setOnClickListener {
            capturePhoto()
        }

        btnSubmitAttendance.setOnClickListener {
            submitAttendance()
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
        // Check if we have camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            // Permission already granted, launch camera
            launchCamera()
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera not available: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, launch camera
                    launchCamera()
                } else {
                    // Permission denied
                    Toast.makeText(
                        this,
                        "Camera permission is required to capture photos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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

    private fun submitAttendance() {
        // Validate inputs
        val fullName = etFullName.text.toString().trim()
        val position = etPosition.text.toString().trim()
        val department = etDepartment.text.toString().trim()

        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            etFullName.requestFocus()
            return
        }

        if (position.isEmpty()) {
            etPosition.error = "Position is required"
            etPosition.requestFocus()
            return
        }

        if (department.isEmpty()) {
            etDepartment.error = "Department/Organization is required"
            etDepartment.requestFocus()
            return
        }

        if (photoFile == null) {
            Toast.makeText(this, "Please capture a photo for verification", Toast.LENGTH_SHORT).show()
            return
        }

        if (agendaId == 0L) {
            showError("Meeting ID is required")
            return
        }

        // Submit attendance with photo
        showLoading(true)

        viewModel.createAttendanceWithPhoto(
            agendaId = agendaId,
            attendeeName = fullName,
            attendeePosition = position,
            attendeeDepartment = department,
            isPresent = true,
            photoFile = photoFile!!
        )
    }

    private fun clearForm() {
        // Clear all input fields
        etFullName.text.clear()
        etPosition.text.clear()
        etDepartment.text.clear()

        // Clear photo
        attendancePhoto = null
        photoFile = null
        ivAttendancePhoto.setImageResource(R.drawable.ic_camera)

        // Reset focus to first field
        etFullName.requestFocus()

        Toast.makeText(this, "Form cleared. Ready for next attendee.", Toast.LENGTH_SHORT).show()
    }
}
