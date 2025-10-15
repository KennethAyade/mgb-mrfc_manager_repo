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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Proponent
import com.mgb.mrfcmanager.utils.DemoData
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

    private lateinit var attendanceAdapter: AttendanceAdapter
    private val attendanceList = mutableListOf<AttendanceItem>()
    private var attendancePhoto: Bitmap? = null

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

        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
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
                Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show()
            }
        }
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
        // TODO: BACKEND - Save attendance records to database
        // TODO: BACKEND - Save attendance photo to storage

        val presentCount = attendanceList.count { it.isPresent }

        if (attendancePhoto == null) {
            Toast.makeText(this, "Please capture attendance photo", Toast.LENGTH_SHORT).show()
            return
        }

        if (presentCount == 0) {
            Toast.makeText(this, "Please mark at least one attendee", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate saving
        Toast.makeText(
            this,
            "Attendance saved: $presentCount present, ${attendanceList.size - presentCount} absent",
            Toast.LENGTH_LONG
        ).show()

        // Return to previous screen or dashboard
        finish()
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
