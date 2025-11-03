package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import com.mgb.mrfcmanager.R

/**
 * Services Menu Activity
 * Shows options for Documents, Notes, and Agenda
 * This is the convergence point for both MRFC and Meeting Management flows
 */
class ServicesMenuActivity : AppCompatActivity() {

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var quarter: String = ""
    private var agendaId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_menu)

        // Get data from intent
        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""
        quarter = intent.getStringExtra("QUARTER") ?: ""
        agendaId = intent.getLongExtra("AGENDA_ID", -1L).takeIf { it != -1L }

        setupToolbar()
        setupViews()
        setupClickListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Services"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvMrfcName).text = mrfcName
        findViewById<TextView>(R.id.tvQuarterInfo).text = quarter
    }

    private fun setupClickListeners() {
        findViewById<MaterialCardView>(R.id.cardDocuments).setOnClickListener {
            val intent = Intent(this, DocumentListActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            intent.putExtra("MRFC_NAME", mrfcName)
            intent.putExtra("QUARTER", quarter)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardNotes).setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            intent.putExtra("MRFC_NAME", mrfcName)
            intent.putExtra("QUARTER", quarter)
            agendaId?.let { intent.putExtra("AGENDA_ID", it) }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardAgenda).setOnClickListener {
            val intent = Intent(this, AgendaViewActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            intent.putExtra("MRFC_NAME", mrfcName)
            intent.putExtra("QUARTER", quarter)
            startActivity(intent)
        }
    }
}
