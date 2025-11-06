package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mgb.mrfcmanager.R

/**
 * CMVR Report Activity
 * Displays Compliance Monitoring Visit Report (CMVR) documents
 * 
 * NOTE: This is a placeholder for future implementation.
 * Will display CMVR-specific documents and monitoring reports once backend is ready.
 */
class CMVRReportActivity : AppCompatActivity() {

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
        supportActionBar?.title = "CMVR Report"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupPlaceholder() {
        findViewById<TextView>(R.id.tvPlaceholderTitle)?.text = "CMVR Report"
        findViewById<TextView>(R.id.tvPlaceholderDescription)?.text =
            "Compliance Monitoring Visit Report (CMVR) will be available here.\n\n" +
            "This feature is currently under development and will show:\n" +
            "• Site visit reports\n" +
            "• Compliance assessments\n" +
            "• Monitoring findings\n" +
            "• Follow-up actions\n\n" +
            "For now, please access CMVR-related documents through the Documents section."
    }
}



