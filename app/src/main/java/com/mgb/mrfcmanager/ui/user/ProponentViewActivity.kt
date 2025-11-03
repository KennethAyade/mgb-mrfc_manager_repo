package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Proponent
import com.mgb.mrfcmanager.utils.DemoData

class ProponentViewActivity : AppCompatActivity() {

    private lateinit var tvProponentName: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvStatus: TextView
    private lateinit var tvContactPerson: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var btnViewAgenda: MaterialButton
    private lateinit var btnViewDocuments: MaterialButton

    private var mrfcId: Long = 0
    private var mrfcName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_view)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        initializeViews()
        setupClickListeners()
        loadProponentData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = mrfcName
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        tvProponentName = findViewById(R.id.tvProponentName)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        cardStatus = findViewById(R.id.cardStatus)
        tvStatus = findViewById(R.id.tvStatus)
        tvContactPerson = findViewById(R.id.tvContactPerson)
        tvContactNumber = findViewById(R.id.tvContactNumber)
        btnViewAgenda = findViewById(R.id.btnViewAgenda)
        btnViewDocuments = findViewById(R.id.btnViewDocuments)
    }

    private fun setupClickListeners() {
        // Both buttons now navigate to quarter selection
        // Following the flowchart: MRFC → Proponent → Quarter → Services
        val navigateToQuarters = {
            val intent = Intent(this, MRFCQuarterSelectionActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            intent.putExtra("MRFC_NAME", mrfcName)
            intent.putExtra("PROPONENT_NAME", tvProponentName.text.toString())
            startActivity(intent)
        }

        btnViewAgenda.setOnClickListener { navigateToQuarters() }
        btnViewDocuments.setOnClickListener { navigateToQuarters() }
    }

    private fun loadProponentData() {
        // TODO: BACKEND - Fetch proponent data from database
        // For now, using demo data - showing first proponent of the selected MRFC
        val proponent = DemoData.proponentList
            .firstOrNull { it.mrfcId == mrfcId }
            ?: DemoData.proponentList.first()

        displayProponentData(proponent)
    }

    private fun displayProponentData(proponent: Proponent) {
        tvProponentName.text = proponent.name
        tvCompanyName.text = proponent.companyName
        tvStatus.text = proponent.status

        // Set status color
        val statusColor = if (proponent.status == "Active") {
            R.color.status_success
        } else {
            R.color.status_pending
        }
        cardStatus.setCardBackgroundColor(getColor(statusColor))

        // Set contact info (from MRFC data for now)
        val mrfc = DemoData.mrfcList.firstOrNull { it.id == mrfcId }
        if (mrfc != null) {
            tvContactPerson.text = mrfc.contactPerson
            tvContactNumber.text = mrfc.contactNumber
        }
    }
}
