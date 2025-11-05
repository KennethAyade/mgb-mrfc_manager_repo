package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mgb.mrfcmanager.R

/**
 * MTF Disbursement Report Activity
 * Displays Multi-Partite Monitoring Team (MTF) disbursement reports
 * 
 * NOTE: This is a placeholder for future implementation.
 * Will display MTF-specific documents and reports once backend is ready.
 */
class MTFDisbursementActivity : AppCompatActivity() {

    private var mrfcId: Long = 0
    private var mrfcName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_placeholder)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        setupPlaceholder()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "MTF Disbursement Report"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupPlaceholder() {
        findViewById<TextView>(R.id.tvPlaceholderTitle)?.text = "MTF Disbursement Report"
        findViewById<TextView>(R.id.tvPlaceholderDescription)?.text =
            "Multi-Partite Monitoring Team (MTF) disbursement reports will be available here.\n\n" +
            "This feature is currently under development and will show:\n" +
            "• MTF fund allocations\n" +
            "• Disbursement schedules\n" +
            "• Utilization reports\n" +
            "• Quarterly summaries\n\n" +
            "For now, please access MTF-related documents through the Documents section."
    }
}

