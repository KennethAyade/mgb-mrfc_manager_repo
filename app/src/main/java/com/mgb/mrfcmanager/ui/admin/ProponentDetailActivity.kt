package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Proponent
import com.mgb.mrfcmanager.utils.DemoData

class ProponentDetailActivity : AppCompatActivity() {

    private lateinit var etProponentName: TextInputEditText
    private lateinit var etCompanyName: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var rgStatus: RadioGroup
    private lateinit var rbActive: MaterialRadioButton
    private lateinit var rbInactive: MaterialRadioButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private var proponentId: Long = -1
    private var mrfcId: Long = -1
    private var currentProponent: Proponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_detail)

        setupToolbar()
        initializeViews()
        loadProponentData()
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
        etProponentName = findViewById(R.id.etProponentName)
        etCompanyName = findViewById(R.id.etCompanyName)
        etNotes = findViewById(R.id.etNotes)
        rgStatus = findViewById(R.id.rgStatus)
        rbActive = findViewById(R.id.rbActive)
        rbInactive = findViewById(R.id.rbInactive)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun loadProponentData() {
        proponentId = intent.getLongExtra("PROPONENT_ID", -1)
        mrfcId = intent.getLongExtra("MRFC_ID", -1)

        if (proponentId == -1L) {
            Toast.makeText(this, "Error loading proponent data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // TODO: BACKEND - Fetch from database
        // For now: Load from demo data
        currentProponent = DemoData.proponentList.find { it.id == proponentId }

        currentProponent?.let { proponent ->
            etProponentName.setText(proponent.name)
            etCompanyName.setText(proponent.companyName)

            // Set status
            when (proponent.status) {
                "Active" -> rbActive.isChecked = true
                "Inactive" -> rbInactive.isChecked = true
            }

            supportActionBar?.title = proponent.name
        }
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveProponentData()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun saveProponentData() {
        val name = etProponentName.text.toString().trim()
        val companyName = etCompanyName.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val status = if (rbActive.isChecked) "Active" else "Inactive"

        if (name.isEmpty() || companyName.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: BACKEND - Save to database
        // For now: Just show success message
        Toast.makeText(this, "Proponent details saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Proponent")
            .setMessage("Are you sure you want to delete this proponent?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProponent()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProponent() {
        // TODO: BACKEND - Delete from database
        // For now: Just show success message
        Toast.makeText(this, "Proponent deleted successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
