package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.MRFC
import com.mgb.mrfcmanager.utils.DemoData

class MRFCDetailActivity : AppCompatActivity() {

    private lateinit var etMRFCName: TextInputEditText
    private lateinit var etMunicipality: TextInputEditText
    private lateinit var etContactPerson: TextInputEditText
    private lateinit var etContactNumber: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnViewProponents: MaterialButton

    private var mrfcId: Long = -1
    private var currentMRFC: MRFC? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrfc_detail)

        setupToolbar()
        initializeViews()
        loadMRFCData()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        etMRFCName = findViewById(R.id.etMRFCName)
        etMunicipality = findViewById(R.id.etMunicipality)
        etContactPerson = findViewById(R.id.etContactPerson)
        etContactNumber = findViewById(R.id.etContactNumber)
        btnSave = findViewById(R.id.btnSave)
        btnViewProponents = findViewById(R.id.btnViewProponents)
    }

    private fun loadMRFCData() {
        mrfcId = intent.getLongExtra("MRFC_ID", -1)

        if (mrfcId == -1L) {
            Toast.makeText(this, "Error loading MRFC data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // TODO: BACKEND - Fetch from database
        // For now: Load from demo data
        currentMRFC = DemoData.mrfcList.find { it.id == mrfcId }

        currentMRFC?.let { mrfc ->
            etMRFCName.setText(mrfc.name)
            etMunicipality.setText(mrfc.municipality)
            etContactPerson.setText(mrfc.contactPerson)
            etContactNumber.setText(mrfc.contactNumber)

            supportActionBar?.title = mrfc.name
        }
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveMRFCData()
        }

        btnViewProponents.setOnClickListener {
            navigateToProponents()
        }
    }

    private fun saveMRFCData() {
        val name = etMRFCName.text.toString().trim()
        val municipality = etMunicipality.text.toString().trim()
        val contactPerson = etContactPerson.text.toString().trim()
        val contactNumber = etContactNumber.text.toString().trim()

        if (name.isEmpty() || municipality.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: BACKEND - Save to database
        // For now: Just show success message
        Toast.makeText(this, "MRFC details saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToProponents() {
        val intent = Intent(this, ProponentListActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NAME", etMRFCName.text.toString())
        startActivity(intent)
    }
}
