package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.R
import java.util.Calendar

/**
 * MRFC Quarter Selection Activity
 * Part of MRFC Management flow: MRFC → Proponent → Quarter → Services
 * After selecting a quarter, navigates to ServicesMenuActivity
 */
class MRFCQuarterSelectionActivity : AppCompatActivity() {

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var proponentName: String = ""
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quarter_selection)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""
        proponentName = intent.getStringExtra("PROPONENT_NAME") ?: ""

        Log.d("MRFCQuarterSelection", "MRFC: $mrfcName, Proponent: $proponentName")

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Quarter"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        findViewById<MaterialCardView>(R.id.cardQ1).setOnClickListener {
            openServicesMenu("1st Quarter $currentYear")
        }
        findViewById<MaterialCardView>(R.id.cardQ2).setOnClickListener {
            openServicesMenu("2nd Quarter $currentYear")
        }
        findViewById<MaterialCardView>(R.id.cardQ3).setOnClickListener {
            openServicesMenu("3rd Quarter $currentYear")
        }
        findViewById<MaterialCardView>(R.id.cardQ4).setOnClickListener {
            openServicesMenu("4th Quarter $currentYear")
        }
    }

    private fun openServicesMenu(quarter: String) {
        Log.d("MRFCQuarterSelection", "Selected quarter: $quarter")
        val intent = Intent(this, ServicesMenuActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NAME", "$mrfcName - $proponentName")
        intent.putExtra("QUARTER", quarter)
        startActivity(intent)
    }
}
