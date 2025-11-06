package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mgb.mrfcmanager.R

/**
 * AEPEP Report Activity
 * Displays Annual Environmental Protection & Enhancement Program (AEPEP) reports
 * 
 * NOTE: This is a placeholder for future implementation.
 * Will display AEPEP-specific documents and environmental reports once backend is ready.
 */
class AEPEPReportActivity : AppCompatActivity() {

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
        supportActionBar?.title = "AEPEP Report"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupPlaceholder() {
        findViewById<TextView>(R.id.tvPlaceholderTitle)?.text = "AEPEP Report"
        findViewById<TextView>(R.id.tvPlaceholderDescription)?.text =
            "Annual Environmental Protection & Enhancement Program (AEPEP) reports will be available here.\n\n" +
            "This feature is currently under development and will show:\n" +
            "• Environmental protection plans\n" +
            "• Enhancement program status\n" +
            "• Annual submissions\n" +
            "• Compliance tracking\n\n" +
            "For now, please access AEPEP-related documents through the Documents section."
    }
}



