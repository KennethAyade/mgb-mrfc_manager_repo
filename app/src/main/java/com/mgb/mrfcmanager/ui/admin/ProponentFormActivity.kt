package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.ProponentApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateProponentRequest
import com.mgb.mrfcmanager.data.remote.dto.UpdateProponentRequest
import com.mgb.mrfcmanager.data.repository.ProponentRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.ProponentDetailState
import com.mgb.mrfcmanager.viewmodel.ProponentViewModel
import com.mgb.mrfcmanager.viewmodel.ProponentViewModelFactory
import kotlinx.coroutines.launch

/**
 * ProponentFormActivity - Create or Edit Proponent
 * Handles both create and update operations
 */
class ProponentFormActivity : AppCompatActivity() {

    private lateinit var etProponentName: TextInputEditText
    private lateinit var etCompanyName: TextInputEditText
    private lateinit var spinnerStatus: AutoCompleteTextView
    private lateinit var etPermitNumber: TextInputEditText
    private lateinit var etPermitType: TextInputEditText
    private lateinit var etContactPerson: TextInputEditText
    private lateinit var etContactNumber: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ProponentViewModel
    private lateinit var repository: ProponentRepository

    private var mrfcId: Long = -1
    private var proponentId: Long? = null // null = create mode, non-null = edit mode
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_form)

        // Get intent data
        mrfcId = intent.getLongExtra("MRFC_ID", -1)
        proponentId = intent.getLongExtra("PROPONENT_ID", -1).takeIf { it != -1L }
        isEditMode = proponentId != null

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupStatusSpinner()
        setupClickListeners()

        if (isEditMode) {
            loadProponentData()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) "Edit Proponent" else "Add Proponent"
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        etProponentName = findViewById(R.id.etProponentName)
        etCompanyName = findViewById(R.id.etCompanyName)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etPermitNumber = findViewById(R.id.etPermitNumber)
        etPermitType = findViewById(R.id.etPermitType)
        etContactPerson = findViewById(R.id.etContactPerson)
        etContactNumber = findViewById(R.id.etContactNumber)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val proponentApiService = retrofit.create(ProponentApiService::class.java)
        repository = ProponentRepository(proponentApiService)
        val factory = ProponentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProponentViewModel::class.java]
    }

    private fun setupStatusSpinner() {
        val statuses = arrayOf("ACTIVE", "INACTIVE", "SUSPENDED")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        spinnerStatus.setAdapter(adapter)
        spinnerStatus.setText("ACTIVE", false)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateFields()) {
                if (isEditMode) {
                    updateProponent()
                } else {
                    createProponent()
                }
            }
        }
    }

    private fun loadProponentData() {
        proponentId?.let { id ->
            viewModel.loadProponentById(id)
            viewModel.proponentDetailState.observe(this) { state ->
                when (state) {
                    is ProponentDetailState.Loading -> showLoading(true)
                    is ProponentDetailState.Success -> {
                        showLoading(false)
                        val proponent = state.data
                        etProponentName.setText(proponent.name)
                        etCompanyName.setText(proponent.companyName)
                        spinnerStatus.setText(proponent.status, false)
                        etPermitNumber.setText(proponent.permitNumber ?: "")
                        etPermitType.setText(proponent.permitType ?: "")
                        etContactPerson.setText(proponent.contactPerson ?: "")
                        etContactNumber.setText(proponent.contactNumber ?: "")
                        etEmail.setText(proponent.email ?: "")
                        etAddress.setText(proponent.address ?: "")
                    }
                    is ProponentDetailState.Error -> {
                        showLoading(false)
                        Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    is ProponentDetailState.Idle -> showLoading(false)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        val name = etProponentName.text.toString().trim()
        val companyName = etCompanyName.text.toString().trim()

        if (name.isEmpty()) {
            etProponentName.error = "Proponent name is required"
            etProponentName.requestFocus()
            return false
        }

        if (companyName.isEmpty()) {
            etCompanyName.error = "Company name is required"
            etCompanyName.requestFocus()
            return false
        }

        if (mrfcId == -1L) {
            Toast.makeText(this, "Invalid MRFC ID", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun createProponent() {
        showLoading(true)

        val request = CreateProponentRequest(
            mrfcId = mrfcId,
            name = etProponentName.text.toString().trim(),
            companyName = etCompanyName.text.toString().trim(),
            status = spinnerStatus.text.toString(),
            permitNumber = etPermitNumber.text.toString().trim().ifEmpty { null },
            permitType = etPermitType.text.toString().trim().ifEmpty { null },
            contactPerson = etContactPerson.text.toString().trim().ifEmpty { null },
            contactNumber = etContactNumber.text.toString().trim().ifEmpty { null },
            email = etEmail.text.toString().trim().ifEmpty { null },
            address = etAddress.text.toString().trim().ifEmpty { null }
        )

        lifecycleScope.launch {
            when (val result = repository.createProponent(request)) {
                is Result.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this@ProponentFormActivity,
                        "Proponent created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this@ProponentFormActivity,
                        "Error: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Result.Loading -> {
                    showLoading(true)
                }
            }
        }
    }

    private fun updateProponent() {
        showLoading(true)

        val request = UpdateProponentRequest(
            name = etProponentName.text.toString().trim(),
            companyName = etCompanyName.text.toString().trim(),
            status = spinnerStatus.text.toString(),
            permitNumber = etPermitNumber.text.toString().trim().ifEmpty { null },
            permitType = etPermitType.text.toString().trim().ifEmpty { null },
            contactPerson = etContactPerson.text.toString().trim().ifEmpty { null },
            contactNumber = etContactNumber.text.toString().trim().ifEmpty { null },
            email = etEmail.text.toString().trim().ifEmpty { null },
            address = etAddress.text.toString().trim().ifEmpty { null }
        )

        proponentId?.let { id ->
            lifecycleScope.launch {
                when (val result = repository.updateProponent(id, request)) {
                    is Result.Success -> {
                        showLoading(false)
                        Toast.makeText(
                            this@ProponentFormActivity,
                            "Proponent updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    is Result.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            this@ProponentFormActivity,
                            "Error: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is Result.Loading -> {
                        showLoading(true)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
        etProponentName.isEnabled = !isLoading
        etCompanyName.isEnabled = !isLoading
        spinnerStatus.isEnabled = !isLoading
        etPermitNumber.isEnabled = !isLoading
        etPermitType.isEnabled = !isLoading
        etContactPerson.isEnabled = !isLoading
        etContactNumber.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etAddress.isEnabled = !isLoading
    }

    companion object {
        const val REQUEST_CODE_CREATE = 1001
        const val REQUEST_CODE_EDIT = 1002
    }
}

