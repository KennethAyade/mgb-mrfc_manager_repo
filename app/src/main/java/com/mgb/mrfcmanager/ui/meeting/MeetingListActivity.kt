package com.mgb.mrfcmanager.ui.meeting

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaRequest
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.AgendaListState
import com.mgb.mrfcmanager.viewmodel.AgendaViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Meeting List Screen
 * Shows all meetings for the selected quarter
 * Allows creating new meetings (Admin/SuperAdmin only)
 */
class MeetingListActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvMeetings: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabCreateMeeting: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: AgendaViewModel
    private lateinit var meetingAdapter: MeetingAdapter

    private val meetings = mutableListOf<AgendaDto>()

    private var mrfcId: Long = 0L
    private var quarter: String = ""
    private var quarterDisplay: String = ""
    private var year: Int = 0
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_list)

        loadIntent()
        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadMeetings()
    }

    private fun loadIntent() {
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)
        quarter = intent.getStringExtra("QUARTER") ?: "Q1"
        quarterDisplay = intent.getStringExtra("QUARTER_DISPLAY") ?: "1st Quarter 2025"
        year = intent.getIntExtra("YEAR", 2025)

        // Get user role from TokenManager
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        isAdmin = userRole == "ADMIN" || userRole == "SUPER_ADMIN"
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Meetings"
            subtitle = quarterDisplay
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initializeViews() {
        rvMeetings = findViewById(R.id.rvMeetings)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        fabCreateMeeting = findViewById(R.id.fabCreateMeeting)
        progressBar = findViewById(R.id.progressBar)

        // Only admins can create meetings
        fabCreateMeeting.visibility = if (isAdmin) View.VISIBLE else View.GONE

        fabCreateMeeting.setOnClickListener {
            createNewMeeting()
        }
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val agendaApiService = retrofit.create(AgendaApiService::class.java)
        val agendaRepository = AgendaRepository(agendaApiService)
        val factory = AgendaViewModelFactory(agendaRepository)
        viewModel = ViewModelProvider(this, factory)[AgendaViewModel::class.java]
    }

    private fun setupRecyclerView() {
        meetingAdapter = MeetingAdapter(meetings) { meeting ->
            openMeetingDetail(meeting)
        }
        rvMeetings.layoutManager = LinearLayoutManager(this)
        rvMeetings.adapter = meetingAdapter
    }

    private fun observeViewModel() {
        viewModel.agendaListState.observe(this) { state ->
            when (state) {
                is AgendaListState.Loading -> {
                    showLoading(true)
                }
                is AgendaListState.Success -> {
                    showLoading(false)
                    updateMeetingsList(state.data)
                }
                is AgendaListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is AgendaListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun loadMeetings() {
        // Load meetings for this quarter
        viewModel.loadAgendas(
            mrfcId = mrfcId,
            quarter = quarter,
            year = year
        )
    }

    private fun updateMeetingsList(agendas: List<AgendaDto>) {
        meetings.clear()
        meetings.addAll(agendas)
        meetingAdapter.notifyDataSetChanged()

        // Show empty state if no meetings
        if (meetings.isEmpty()) {
            rvMeetings.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvMeetings.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun createNewMeeting() {
        // Show date picker to select meeting date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Calculate date range for the selected quarter
        val (startDate, endDate) = getQuarterDateRange()

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val meetingDate = dateFormat.format(calendar.time)

                // Validate that the selected date falls within the quarter
                if (!isDateInQuarter(calendar, startDate, endDate)) {
                    Toast.makeText(
                        this,
                        "Meeting date must be within $quarterDisplay (${formatDateForDisplay(startDate)} - ${formatDateForDisplay(endDate)})",
                        Toast.LENGTH_LONG
                    ).show()
                    return@DatePickerDialog
                }

                // Create agenda via ViewModel
                val agendaRequest = CreateAgendaRequest(
                    mrfcId = if (mrfcId == 0L) null else mrfcId, // null for general meetings
                    quarterId = getQuarterId(),
                    meetingDate = meetingDate,
                    meetingTime = null,
                    location = null,
                    status = "DRAFT"
                )

                showLoading(true)
                lifecycleScope.launch {
                    when (val result = viewModel.createAgenda(agendaRequest)) {
                        is Result.Success -> {
                            showLoading(false)
                            Toast.makeText(
                                this@MeetingListActivity,
                                "Meeting created successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to meeting detail
                            val intent = Intent(this@MeetingListActivity, MeetingDetailActivity::class.java).apply {
                                putExtra("AGENDA_ID", result.data.id)
                                putExtra("MEETING_TITLE", "Meeting #${result.data.id}")
                                putExtra("MRFC_ID", mrfcId)
                            }
                            startActivity(intent)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            showError(result.message)
                        }
                        is Result.Loading -> {
                            // Already showing loading
                        }
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getQuarterDateRange(): Pair<Calendar, Calendar> {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()

        when (quarter) {
            "Q1" -> {
                startDate.set(year, Calendar.JANUARY, 1)
                endDate.set(year, Calendar.MARCH, 31)
            }
            "Q2" -> {
                startDate.set(year, Calendar.APRIL, 1)
                endDate.set(year, Calendar.JUNE, 30)
            }
            "Q3" -> {
                startDate.set(year, Calendar.JULY, 1)
                endDate.set(year, Calendar.SEPTEMBER, 30)
            }
            "Q4" -> {
                startDate.set(year, Calendar.OCTOBER, 1)
                endDate.set(year, Calendar.DECEMBER, 31)
            }
        }

        return Pair(startDate, endDate)
    }

    private fun isDateInQuarter(selectedDate: Calendar, startDate: Calendar, endDate: Calendar): Boolean {
        return selectedDate.timeInMillis >= startDate.timeInMillis &&
               selectedDate.timeInMillis <= endDate.timeInMillis
    }

    private fun formatDateForDisplay(date: Calendar): String {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return format.format(date.time)
    }

    private fun openMeetingDetail(meeting: AgendaDto) {
        // TODO: Create MeetingDetailActivity
        val intent = Intent(this, MeetingDetailActivity::class.java).apply {
            putExtra("AGENDA_ID", meeting.id)
            putExtra("MEETING_TITLE", "Meeting ${meeting.id}")
            putExtra("MRFC_ID", mrfcId)
        }
        startActivity(intent)
    }

    private fun getQuarterId(): Long {
        // TODO: BACKEND - Get actual quarter ID from backend
        // For now, return a placeholder based on quarter
        return when (quarter) {
            "Q1" -> 1L
            "Q2" -> 2L
            "Q3" -> 3L
            "Q4" -> 4L
            else -> 1L
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload meetings when returning from create/edit screen
        loadMeetings()
    }
}

/**
 * Adapter for displaying meetings in RecyclerView
 */
class MeetingAdapter(
    private val meetings: List<AgendaDto>,
    private val onMeetingClick: (AgendaDto) -> Unit
) : RecyclerView.Adapter<MeetingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meeting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(meetings[position], onMeetingClick)
    }

    override fun getItemCount() = meetings.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMeetingTitle: TextView = itemView.findViewById(R.id.tvMeetingTitle)
        private val tvMeetingDate: TextView = itemView.findViewById(R.id.tvMeetingDate)
        private val tvMeetingStatus: TextView = itemView.findViewById(R.id.tvMeetingStatus)
        private val tvMeetingLocation: TextView = itemView.findViewById(R.id.tvMeetingLocation)

        private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun bind(meeting: AgendaDto, onClick: (AgendaDto) -> Unit) {
            tvMeetingTitle.text = "Meeting #${meeting.id}"

            // Format date
            meeting.meetingDate?.let {
                try {
                    val parsedDate = apiDateFormat.parse(it)
                    tvMeetingDate.text = parsedDate?.let { date -> dateFormat.format(date) } ?: it
                } catch (e: Exception) {
                    tvMeetingDate.text = it
                }
            } ?: run {
                tvMeetingDate.text = "Date not set"
            }

            // Show status
            tvMeetingStatus.text = meeting.status

            // Show location
            tvMeetingLocation.text = meeting.location ?: "Location TBD"

            itemView.setOnClickListener { onClick(meeting) }
        }
    }
}
