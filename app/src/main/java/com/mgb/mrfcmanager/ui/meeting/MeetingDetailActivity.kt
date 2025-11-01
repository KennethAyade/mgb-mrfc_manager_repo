package com.mgb.mrfcmanager.ui.meeting

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.viewmodel.AgendaDetailState
import com.mgb.mrfcmanager.viewmodel.AgendaViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaViewModelFactory

/**
 * Meeting Detail Screen
 * Shows meeting details with 3 tabs:
 * 1. Agenda - View/add agenda items (all users)
 * 2. Attendance - Log attendance with photo (all users)
 * 3. Minutes - View/edit minutes (organizer only)
 */
class MeetingDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvMeetingTitle: TextView
    private lateinit var tvMeetingDate: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: AgendaViewModel
    private lateinit var pagerAdapter: MeetingDetailPagerAdapter

    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L
    private var meetingTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)

        loadIntent()
        setupToolbar()
        initializeViews()
        setupViewModel()
        setupViewPager()
        observeViewModel()
        loadMeetingDetails()
    }

    private fun loadIntent() {
        agendaId = intent.getLongExtra("AGENDA_ID", 0L)
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)
        meetingTitle = intent.getStringExtra("MEETING_TITLE") ?: "Meeting Details"

        if (agendaId == 0L) {
            Toast.makeText(this, "Invalid meeting ID", Toast.LENGTH_SHORT).show()
            finish()
        }
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
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val agendaApiService = retrofit.create(AgendaApiService::class.java)
        val agendaRepository = AgendaRepository(agendaApiService)
        val factory = AgendaViewModelFactory(agendaRepository)
        viewModel = ViewModelProvider(this, factory)[AgendaViewModel::class.java]
    }

    private fun setupViewPager() {
        pagerAdapter = MeetingDetailPagerAdapter(this, agendaId, mrfcId)
        viewPager.adapter = pagerAdapter

        // Connect tabs with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Agenda"
                1 -> "Attendance"
                2 -> "Minutes"
                else -> "Tab $position"
            }

            // Add icons to tabs
            tab.icon = when (position) {
                0 -> getDrawable(R.drawable.ic_note)
                1 -> getDrawable(R.drawable.ic_people)
                2 -> getDrawable(R.drawable.ic_document)
                else -> null
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
                    val agenda = state.data
                    tvMeetingTitle.text = "Meeting #${agenda.id}"

                    // Format and display meeting date
                    agenda.meetingDate?.let {
                        tvMeetingDate.text = "Date: $it"
                        tvMeetingDate.visibility = View.VISIBLE
                    } ?: run {
                        tvMeetingDate.visibility = View.GONE
                    }
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

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
