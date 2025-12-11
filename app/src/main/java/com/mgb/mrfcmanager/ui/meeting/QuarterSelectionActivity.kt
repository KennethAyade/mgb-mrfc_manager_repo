package com.mgb.mrfcmanager.ui.meeting

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.mgb.mrfcmanager.R
import java.util.Calendar

/**
 * Quarter Selection Screen
 * First step in the meeting management flow
 * Users select a quarter (Q1-Q4) to view/manage meetings
 */
class QuarterSelectionActivity : AppCompatActivity() {

    private lateinit var cardQ1: CardView
    private lateinit var cardQ2: CardView
    private lateinit var cardQ3: CardView
    private lateinit var cardQ4: CardView

    // Year selector views
    private lateinit var btnPreviousYear: ImageButton
    private lateinit var btnNextYear: ImageButton
    private lateinit var tvSelectedYear: TextView
    private lateinit var tvQ1Year: TextView
    private lateinit var tvQ2Year: TextView
    private lateinit var tvQ3Year: TextView
    private lateinit var tvQ4Year: TextView

    private var mrfcId: Long = 0L
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    // Year range constraints
    private val minYear = 2020
    private val maxYear = 2030

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quarter_selection)

        // Get MRFC ID from intent (0 means general meetings, not tied to specific MRFC)
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)

        setupToolbar()
        initializeViews()
        setupClickListeners()
        setupYearSelector()
        updateYearDisplay()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Select Quarter"
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initializeViews() {
        cardQ1 = findViewById(R.id.cardQ1)
        cardQ2 = findViewById(R.id.cardQ2)
        cardQ3 = findViewById(R.id.cardQ3)
        cardQ4 = findViewById(R.id.cardQ4)

        // Year selector views
        btnPreviousYear = findViewById(R.id.btnPreviousYear)
        btnNextYear = findViewById(R.id.btnNextYear)
        tvSelectedYear = findViewById(R.id.tvSelectedYear)
        tvQ1Year = findViewById(R.id.tvQ1Year)
        tvQ2Year = findViewById(R.id.tvQ2Year)
        tvQ3Year = findViewById(R.id.tvQ3Year)
        tvQ4Year = findViewById(R.id.tvQ4Year)
    }

    private fun setupClickListeners() {
        cardQ1.setOnClickListener { openMeetingList("Q1", "1st Quarter") }
        cardQ2.setOnClickListener { openMeetingList("Q2", "2nd Quarter") }
        cardQ3.setOnClickListener { openMeetingList("Q3", "3rd Quarter") }
        cardQ4.setOnClickListener { openMeetingList("Q4", "4th Quarter") }
    }

    private fun setupYearSelector() {
        btnPreviousYear.setOnClickListener {
            if (selectedYear > minYear) {
                selectedYear--
                updateYearDisplay()
            }
        }

        btnNextYear.setOnClickListener {
            if (selectedYear < maxYear) {
                selectedYear++
                updateYearDisplay()
            }
        }
    }

    private fun updateYearDisplay() {
        val yearStr = selectedYear.toString()
        tvSelectedYear.text = yearStr
        tvQ1Year.text = yearStr
        tvQ2Year.text = yearStr
        tvQ3Year.text = yearStr
        tvQ4Year.text = yearStr

        // Update button states
        btnPreviousYear.alpha = if (selectedYear > minYear) 1.0f else 0.3f
        btnNextYear.alpha = if (selectedYear < maxYear) 1.0f else 0.3f
    }

    private fun openMeetingList(quarter: String, quarterDisplay: String) {
        val intent = Intent(this, MeetingListActivity::class.java).apply {
            putExtra("MRFC_ID", mrfcId)
            putExtra("QUARTER", quarter)
            putExtra("QUARTER_DISPLAY", "$quarterDisplay $selectedYear")
            putExtra("YEAR", selectedYear)
        }
        startActivity(intent)
    }
}
