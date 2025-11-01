package com.mgb.mrfcmanager.ui.meeting

import android.content.Intent
import android.os.Bundle
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

    private var mrfcId: Long = 0L
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quarter_selection)

        // Get MRFC ID from intent (0 means general meetings, not tied to specific MRFC)
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)

        setupToolbar()
        initializeViews()
        setupClickListeners()
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
    }

    private fun setupClickListeners() {
        cardQ1.setOnClickListener { openMeetingList("Q1", "1st Quarter") }
        cardQ2.setOnClickListener { openMeetingList("Q2", "2nd Quarter") }
        cardQ3.setOnClickListener { openMeetingList("Q3", "3rd Quarter") }
        cardQ4.setOnClickListener { openMeetingList("Q4", "4th Quarter") }
    }

    private fun openMeetingList(quarter: String, quarterDisplay: String) {
        val intent = Intent(this, MeetingListActivity::class.java).apply {
            putExtra("MRFC_ID", mrfcId)
            putExtra("QUARTER", quarter)
            putExtra("QUARTER_DISPLAY", "$quarterDisplay $currentYear")
            putExtra("YEAR", currentYear)
        }
        startActivity(intent)
    }
}
