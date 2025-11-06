package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mgb.mrfcmanager.R

/**
 * Research Accomplishments Activity
 * Displays research accomplishments and studies related to mining operations
 * 
 * NOTE: This is a placeholder for future implementation.
 * Will display research reports and accomplishments once backend is ready.
 */
class ResearchAccomplishmentsActivity : AppCompatActivity() {

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
        supportActionBar?.title = "Research Accomplishments"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupPlaceholder() {
        findViewById<TextView>(R.id.tvPlaceholderTitle)?.text = "Research Accomplishments"
        findViewById<TextView>(R.id.tvPlaceholderDescription)?.text =
            "Research accomplishments and studies will be available here.\n\n" +
            "This feature is currently under development and will show:\n" +
            "• Mining research reports\n" +
            "• Environmental studies\n" +
            "• Safety research\n" +
            "• Technical accomplishments\n\n" +
            "For now, please access research-related documents through the Documents section."
    }
}



