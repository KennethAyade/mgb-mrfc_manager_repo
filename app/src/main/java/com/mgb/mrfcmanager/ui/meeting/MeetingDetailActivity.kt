package com.mgb.mrfcmanager.ui.meeting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaRequest
import com.mgb.mrfcmanager.ui.user.NotesActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.ui.base.BaseActivity
import com.mgb.mrfcmanager.viewmodel.AgendaDetailState
import com.mgb.mrfcmanager.viewmodel.AgendaViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaViewModelFactory
import com.mgb.mrfcmanager.viewmodel.MeetingRealtimeViewModel

/**
 * Meeting Detail Screen
 * Shows meeting details with 4 tabs:
 * 1. Attendance - Log attendance with photo (all users)
 * 2. Agenda - View approved agenda items (all users)
 * 3. Other Matters - View/approve other matters (admins see all, users see own)
 * 4. Minutes - View/edit minutes (organizer only)
 */
class MeetingDetailActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvMeetingTitle: TextView
    private lateinit var tvMeetingDate: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar

    // Timer views
    private lateinit var layoutTimer: LinearLayout
    private lateinit var tvTimerStatus: TextView
    private lateinit var btnStartMeeting: MaterialButton
    private lateinit var btnEndMeeting: MaterialButton

    private lateinit var viewModel: AgendaViewModel
    private lateinit var realtimeViewModel: MeetingRealtimeViewModel
    private lateinit var pagerAdapter: MeetingDetailPagerAdapter

    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L
    private var meetingTitle: String = ""
    private var currentAgenda: AgendaDto? = null

    // Timer handler for real-time updates
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    // For role-based menu visibility
    private var isAdminOrSuperAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)

        loadIntent()
        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRealtime()
        setupViewPager()
        setupTimerSection()
        observeViewModel()
        loadMeetingDetails()

        // Setup floating home button
        setupHomeFab()
    }

    private fun loadIntent() {
        agendaId = intent.getLongExtra("AGENDA_ID", 0L)
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)
        meetingTitle = intent.getStringExtra("MEETING_TITLE") ?: "Meeting Details"

        if (agendaId == 0L) {
            Toast.makeText(this, "Invalid meeting ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Check user role for menu visibility
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        isAdminOrSuperAdmin = userRole == "ADMIN" || userRole == "SUPER_ADMIN"
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = meetingTitle
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initializeViews() {
        tvMeetingTitle = findViewById(R.id.tvMeetingTitle)
        tvMeetingDate = findViewById(R.id.tvMeetingDate)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)

        // Timer views
        layoutTimer = findViewById(R.id.layoutTimer)
        tvTimerStatus = findViewById(R.id.tvTimerStatus)
        btnStartMeeting = findViewById(R.id.btnStartMeeting)
        btnEndMeeting = findViewById(R.id.btnEndMeeting)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val agendaApiService = retrofit.create(AgendaApiService::class.java)
        val agendaRepository = AgendaRepository(agendaApiService)
        val factory = AgendaViewModelFactory(agendaRepository)
        viewModel = ViewModelProvider(this, factory)[AgendaViewModel::class.java]
    }

    private fun setupRealtime() {
        realtimeViewModel = ViewModelProvider(this)[MeetingRealtimeViewModel::class.java]
    }

    private fun setupViewPager() {
        // Show 5 tabs for Admin/SuperAdmin (includes Recordings), 4 tabs for regular users
        // NOTE: Proposals tab has been removed per requirements
        val tabCount = if (isAdminOrSuperAdmin) 5 else 4
        pagerAdapter = MeetingDetailPagerAdapter(this, agendaId, mrfcId, tabCount)
        viewPager.adapter = pagerAdapter

        // IMPORTANT: Keep all meeting tabs in memory.
        // Rationale: Voice recording is currently managed in VoiceRecordingFragment via MediaRecorder.
        // If ViewPager2 destroys/recreates fragments while switching tabs, the recording can be interrupted.
        // Keeping all tabs alive prevents fragment teardown during in-meeting navigation.
        viewPager.offscreenPageLimit = tabCount

        // Connect tabs with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Attendance"
                1 -> "Agenda"
                2 -> "Other Matters"
                3 -> "Minutes"
                4 -> if (isAdminOrSuperAdmin) "Recordings" else ""
                else -> "Tab $position"
            }

            // Add icons to tabs with distinct colors
            val iconRes = when (position) {
                0 -> R.drawable.ic_people
                1 -> R.drawable.ic_note
                2 -> R.drawable.ic_list
                3 -> R.drawable.ic_document
                4 -> if (isAdminOrSuperAdmin) R.drawable.ic_mic else null
                else -> null
            }

            val colorRes = when (position) {
                0 -> R.color.tab_icon_attendance
                1 -> R.color.tab_icon_agenda
                2 -> R.color.tab_icon_other_matters
                3 -> R.color.tab_icon_minutes
                4 -> if (isAdminOrSuperAdmin) R.color.tab_icon_recordings else null
                else -> null
            }

            if (iconRes != null && colorRes != null) {
                val icon = ContextCompat.getDrawable(this, iconRes)?.mutate()
                icon?.let {
                    val wrappedIcon = DrawableCompat.wrap(it)
                    DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorRes))
                    tab.icon = wrappedIcon
                }
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewModel.agendaDetailState.observe(this) { state ->
            when (state) {
                is AgendaDetailState.Loading -> {
                    showLoading(true)
                }
                is AgendaDetailState.Success -> {
                    showLoading(false)
                    currentAgenda = state.data
                    val agenda = state.data

                    // Show meeting title or fallback
                    tvMeetingTitle.text = agenda.meetingTitle?.takeIf { it.isNotBlank() }
                        ?: "Meeting #${agenda.id}"

                    // Update toolbar title as well
                    supportActionBar?.title = tvMeetingTitle.text

                    // Format and display meeting date with location
                    val dateStr = agenda.meetingDate ?: "Date not set"
                    val locationStr = agenda.location?.takeIf { it.isNotBlank() }

                    if (locationStr != null) {
                        tvMeetingDate.text = "Date: $dateStr | Location: $locationStr"
                    } else {
                        tvMeetingDate.text = "Date: $dateStr"
                    }
                    tvMeetingDate.visibility = View.VISIBLE

                    // Update timer UI
                    updateTimerUI(agenda)
                }
                is AgendaDetailState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is AgendaDetailState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun loadMeetingDetails() {
        viewModel.loadAgendaById(agendaId)
    }

    private fun setupTimerSection() {
        // Show timer section only for ADMIN and SUPER_ADMIN
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()

        if (userRole == "ADMIN" || userRole == "SUPER_ADMIN") {
            layoutTimer.visibility = View.VISIBLE

            // Setup button click listeners
            btnStartMeeting.setOnClickListener {
                startMeeting()
            }

            btnEndMeeting.setOnClickListener {
                endMeeting()
            }
        } else {
            layoutTimer.visibility = View.GONE
        }
    }

    private fun updateTimerUI(agenda: AgendaDto) {
        // Stop any existing timer updates
        timerRunnable?.let { timerHandler.removeCallbacks(it) }

        when {
            // Meeting has ended
            agenda.actualEndTime != null && agenda.actualStartTime != null -> {
                val duration = agenda.durationMinutes ?: 0
                tvTimerStatus.text = "Meeting Completed: ${formatDuration(duration)}"
                btnStartMeeting.isEnabled = false
                btnEndMeeting.isEnabled = false
            }
            // Meeting is in progress
            agenda.actualStartTime != null && agenda.actualEndTime == null -> {
                btnStartMeeting.isEnabled = false
                btnEndMeeting.isEnabled = true

                // Start real-time timer updates
                timerRunnable = object : Runnable {
                    override fun run() {
                        try {
                            val startTime = parseISOTimestamp(agenda.actualStartTime)
                            val currentTime = System.currentTimeMillis()
                            val elapsedMinutes = ((currentTime - startTime) / 60000).toInt()
                            tvTimerStatus.text = "Meeting In Progress: ${formatDuration(elapsedMinutes)}"

                            // Update every minute
                            timerHandler.postDelayed(this, 60000)
                        } catch (e: Exception) {
                            tvTimerStatus.text = "Meeting In Progress"
                        }
                    }
                }
                timerRunnable?.run()
            }
            // Meeting not started
            else -> {
                tvTimerStatus.text = "Meeting Not Started"
                btnStartMeeting.isEnabled = true
                btnEndMeeting.isEnabled = false
            }
        }
    }

    private fun startMeeting() {
        lifecycleScope.launch {
            try {
                val tokenManager = MRFCManagerApp.getTokenManager()
                val retrofit = RetrofitClient.getInstance(tokenManager)
                val apiService = retrofit.create(AgendaApiService::class.java)

                val response = apiService.startMeeting(agendaId)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MeetingDetailActivity, "Meeting started successfully", Toast.LENGTH_SHORT).show()
                    // Reload meeting details to get updated timer info
                    loadMeetingDetails()
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Failed to start meeting"
                    Toast.makeText(this@MeetingDetailActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MeetingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun endMeeting() {
        lifecycleScope.launch {
            try {
                val tokenManager = MRFCManagerApp.getTokenManager()
                val retrofit = RetrofitClient.getInstance(tokenManager)
                val apiService = retrofit.create(AgendaApiService::class.java)

                val response = apiService.endMeeting(agendaId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val duration = response.body()?.data?.durationMinutes ?: 0
                    Toast.makeText(
                        this@MeetingDetailActivity,
                        "Meeting ended. Duration: ${formatDuration(duration)}",
                        Toast.LENGTH_LONG
                    ).show()
                    // Reload meeting details to get updated timer info
                    loadMeetingDetails()
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Failed to end meeting"
                    Toast.makeText(this@MeetingDetailActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MeetingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m"
        }
    }

    private fun parseISOTimestamp(timestamp: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(timestamp)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            // Try without milliseconds
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format.parse(timestamp)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        // Start meeting realtime stream (SSE) while this screen is visible
        realtimeViewModel.start(agendaId)
    }

    override fun onStop() {
        super.onStop()
        // Stop SSE stream when leaving the meeting screen to avoid leaks
        realtimeViewModel.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop timer updates when activity is destroyed
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    // ==================== Menu Handling ====================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Show menu for all users (Notes is for everyone, Edit/Delete only for admins)
        menuInflater.inflate(R.menu.menu_meeting_detail, menu)

        // Hide Edit and Delete for non-admin users
        if (!isAdminOrSuperAdmin) {
            menu.findItem(R.id.action_edit)?.isVisible = false
            menu.findItem(R.id.action_delete)?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notes -> {
                openMyNotes()
                true
            }
            R.id.action_edit -> {
                showEditMeetingDialog()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openMyNotes() {
        val intent = Intent(this, NotesActivity::class.java).apply {
            putExtra("AGENDA_ID", agendaId)
            putExtra("MRFC_ID", mrfcId)
            putExtra("MEETING_TITLE", currentAgenda?.meetingTitle ?: meetingTitle)
        }
        startActivity(intent)
    }

    // ==================== Edit Meeting ====================

    private fun showEditMeetingDialog() {
        val agenda = currentAgenda ?: run {
            Toast.makeText(this, "Meeting data not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_meeting, null)

        // Get views
        val etMeetingTitle = dialogView.findViewById<TextInputEditText>(R.id.etMeetingTitle)
        val etMeetingDate = dialogView.findViewById<TextInputEditText>(R.id.etMeetingDate)
        val etLocation = dialogView.findViewById<TextInputEditText>(R.id.etLocation)
        val etMeetingTime = dialogView.findViewById<TextInputEditText>(R.id.etMeetingTime)
        val actvStatus = dialogView.findViewById<AutoCompleteTextView>(R.id.actvStatus)

        // Populate current values
        etMeetingTitle.setText(agenda.meetingTitle ?: "")
        etMeetingDate.setText(agenda.meetingDate ?: "")
        etLocation.setText(agenda.location ?: "")
        etMeetingTime.setText(agenda.meetingTime ?: "")

        // Setup status dropdown
        val statuses = arrayOf("DRAFT", "PUBLISHED", "COMPLETED", "CANCELLED")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        actvStatus.setAdapter(statusAdapter)
        actvStatus.setText(agenda.status ?: "DRAFT", false)

        // Setup date picker
        etMeetingDate.setOnClickListener {
            showDatePicker(etMeetingDate, agenda.meetingDate)
        }

        // Setup time picker
        etMeetingTime.setOnClickListener {
            showTimePicker(etMeetingTime, agenda.meetingTime)
        }

        // Show dialog
        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Meeting")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedDate = etMeetingDate.text.toString().trim()
                if (updatedDate.isEmpty()) {
                    Toast.makeText(this, "Meeting date is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = CreateAgendaRequest(
                    mrfcId = agenda.mrfc?.id,
                    quarterId = agenda.quarter?.id ?: 1L,
                    meetingTitle = etMeetingTitle.text.toString().trim().ifEmpty { null },
                    meetingDate = updatedDate,
                    meetingTime = etMeetingTime.text.toString().trim().ifEmpty { null },
                    location = etLocation.text.toString().trim().ifEmpty { null },
                    status = actvStatus.text.toString()
                )

                updateMeeting(request)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(editText: TextInputEditText, currentDate: String?) {
        val calendar = Calendar.getInstance()

        // Parse current date if available
        currentDate?.let {
            try {
                val parts = it.split("-")
                if (parts.size == 3) {
                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (e: Exception) {
                // Use current date
            }
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                editText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: TextInputEditText, currentTime: String?) {
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        // Parse current time if available (format: "09:00 AM" or "14:30")
        currentTime?.let {
            try {
                val parts = it.replace(" AM", "").replace(" PM", "").split(":")
                if (parts.size >= 2) {
                    hour = parts[0].toInt()
                    minute = parts[1].toInt()
                    // Adjust for PM
                    if (it.contains("PM") && hour < 12) hour += 12
                    if (it.contains("AM") && hour == 12) hour = 0
                }
            } catch (e: Exception) {
                // Use current time
            }
        }

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val displayHour = when {
                    selectedHour == 0 -> 12
                    selectedHour > 12 -> selectedHour - 12
                    else -> selectedHour
                }
                val formattedTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
                editText.setText(formattedTime)
            },
            hour,
            minute,
            false // 12-hour format
        ).show()
    }

    private fun updateMeeting(request: CreateAgendaRequest) {
        lifecycleScope.launch {
            try {
                val tokenManager = MRFCManagerApp.getTokenManager()
                val retrofit = RetrofitClient.getInstance(tokenManager)
                val apiService = retrofit.create(AgendaApiService::class.java)

                val response = apiService.updateAgenda(agendaId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MeetingDetailActivity, "Meeting updated successfully", Toast.LENGTH_SHORT).show()
                    // Reload meeting details
                    loadMeetingDetails()
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Failed to update meeting"
                    Toast.makeText(this@MeetingDetailActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MeetingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== Delete Meeting ====================

    private fun showDeleteConfirmationDialog() {
        val agenda = currentAgenda ?: run {
            Toast.makeText(this, "Meeting data not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if meeting is completed
        if (agenda.status == "COMPLETED") {
            Toast.makeText(this, "Cannot delete completed meetings", Toast.LENGTH_LONG).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Meeting")
            .setMessage("Are you sure you want to delete this meeting?\n\nThis will also delete all attendance records and other data associated with this meeting.\n\nThis action cannot be undone.")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Delete") { _, _ ->
                deleteMeeting()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMeeting() {
        lifecycleScope.launch {
            try {
                val tokenManager = MRFCManagerApp.getTokenManager()
                val retrofit = RetrofitClient.getInstance(tokenManager)
                val apiService = retrofit.create(AgendaApiService::class.java)

                val response = apiService.deleteAgenda(agendaId)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MeetingDetailActivity, "Meeting deleted successfully", Toast.LENGTH_SHORT).show()
                    // Close this activity and return to list
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Failed to delete meeting"
                    Toast.makeText(this@MeetingDetailActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MeetingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
