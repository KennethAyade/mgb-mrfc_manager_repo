package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.mgb.mrfcmanager.ui.base.BaseActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateMrfcRequest
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.viewmodel.MrfcCreateState
import com.mgb.mrfcmanager.viewmodel.MrfcViewModel
import com.mgb.mrfcmanager.viewmodel.MrfcViewModelFactory

/**
 * Create MRFC Activity - Form for creating new MRFCs
 * Integrated with backend API
 */
class CreateMRFCActivity : BaseActivity() {

    private lateinit var etMRFCName: TextInputEditText
    private lateinit var etMrfcCode: TextInputEditText
    private lateinit var etMunicipality: TextInputEditText
    private lateinit var etProvince: TextInputEditText
    private lateinit var etRegion: TextInputEditText
    private lateinit var etContactPerson: TextInputEditText
    private lateinit var etContactNumber: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnCreate: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: MrfcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_mrfc)

        setupToolbar()
        initializeViews()
        setupViewModel()
        observeCreateState()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Create New MRFC"
        }
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        etMRFCName = findViewById(R.id.etMRFCName)
        etMrfcCode = findViewById(R.id.etMrfcCode)
        etMunicipality = findViewById(R.id.etMunicipality)
        etProvince = findViewById(R.id.etProvince)
        etRegion = findViewById(R.id.etRegion)
        etContactPerson = findViewById(R.id.etContactPerson)
        etContactNumber = findViewById(R.id.etContactNumber)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        btnCreate = findViewById(R.id.btnCreate)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val mrfcApiService = retrofit.create(MrfcApiService::class.java)
        val repository = MrfcRepository(mrfcApiService)

        val factory = MrfcViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MrfcViewModel::class.java]
    }

    private fun observeCreateState() {
        viewModel.mrfcCreateState.observe(this) { state ->
            when (state) {
                is MrfcCreateState.Idle -> {
                    showLoading(false)
                }
                is MrfcCreateState.Loading -> {
                    showLoading(true)
                }
                is MrfcCreateState.Success -> {
                    showLoading(false)
                    showSuccess("MRFC created successfully") {
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                is MrfcCreateState.Error -> {
                    showLoading(false)
                    showError(state.message, "Failed to Create MRFC")
                }
            }
        }
    }

    private fun setupListeners() {
        btnCreate.setOnClickListener {
            createMRFC()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createMRFC() {
        val name = etMRFCName.text.toString().trim()
        val mrfcCode = etMrfcCode.text.toString().trim()
        val municipality = etMunicipality.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val region = etRegion.text.toString().trim()
        val contactPerson = etContactPerson.text.toString().trim()
        val contactNumber = etContactNumber.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val address = etAddress.text.toString().trim()

        // Validation
        if (!validateFields(name, municipality, province, region, contactPerson, contactNumber)) {
            return
        }

        // Create request (category field removed as it's not in CreateMrfcRequest DTO)
        val request = CreateMrfcRequest(
            name = name,
            mrfcCode = mrfcCode.ifEmpty { null },
            municipality = municipality,
            province = province,
            region = region,
            contactPerson = contactPerson,
            contactNumber = contactNumber,
            email = email.ifEmpty { null },
            address = address.ifEmpty { null }
        )

        // Call ViewModel to create MRFC
        viewModel.createMrfc(request)
    }

    private fun validateFields(
        name: String,
        municipality: String,
        province: String,
        region: String,
        contactPerson: String,
        contactNumber: String
    ): Boolean {
        if (name.isEmpty()) {
            etMRFCName.error = "MRFC Name is required"
            etMRFCName.requestFocus()
            return false
        }

        if (municipality.isEmpty()) {
            etMunicipality.error = "Municipality is required"
            etMunicipality.requestFocus()
            return false
        }

        if (province.isEmpty()) {
            etProvince.error = "Province is required"
            etProvince.requestFocus()
            return false
        }

        if (region.isEmpty()) {
            etRegion.error = "Region is required"
            etRegion.requestFocus()
            return false
        }

        if (contactPerson.isEmpty()) {
            etContactPerson.error = "Contact Person is required"
            etContactPerson.requestFocus()
            return false
        }

        if (contactNumber.isEmpty()) {
            etContactNumber.error = "Contact Number is required"
            etContactNumber.requestFocus()
            return false
        }

        // Validate contact number format (Philippine format)
        if (!contactNumber.matches(Regex("^(\\+63|0)\\d{10}$"))) {
            etContactNumber.error = "Invalid format. Use +639XXXXXXXXX or 09XXXXXXXXX"
            etContactNumber.requestFocus()
            return false
        }

        // Validate email if provided
        val email = etEmail.text.toString().trim()
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            etEmail.requestFocus()
            return false
        }

        return true
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnCreate.isEnabled = !isLoading
        btnCancel.isEnabled = !isLoading

        // Disable all input fields while loading
        etMRFCName.isEnabled = !isLoading
        etMrfcCode.isEnabled = !isLoading
        etMunicipality.isEnabled = !isLoading
        etProvince.isEnabled = !isLoading
        etRegion.isEnabled = !isLoading
        etContactPerson.isEnabled = !isLoading
        etContactNumber.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etAddress.isEnabled = !isLoading
    }

    companion object {
        const val REQUEST_CODE_CREATE_MRFC = 100
    }
}
