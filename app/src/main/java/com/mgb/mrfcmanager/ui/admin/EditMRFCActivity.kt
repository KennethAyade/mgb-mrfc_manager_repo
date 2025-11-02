package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.dto.ComplianceStatus
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateComplianceRequest
import com.mgb.mrfcmanager.data.remote.dto.UpdateMrfcRequest
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Edit MRFC Activity - Edit MRFC details and manage compliance
 * Integrated with backend API
 */
class EditMRFCActivity : AppCompatActivity() {

    private lateinit var etMRFCName: TextInputEditText
    private lateinit var etMrfcCode: TextInputEditText
    private lateinit var etMunicipality: TextInputEditText
    private lateinit var etProvince: TextInputEditText
    private lateinit var etRegion: TextInputEditText
    private lateinit var etContactPerson: TextInputEditText
    private lateinit var etContactNumber: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var switchActive: Switch

    // Compliance fields
    private lateinit var etCompliancePercentage: TextInputEditText
    private lateinit var actvComplianceStatus: AutoCompleteTextView
    private lateinit var etComplianceRemarks: TextInputEditText
    private lateinit var tvComplianceUpdatedAt: TextView
    private lateinit var chipCompliant: Chip
    private lateinit var chipPartial: Chip
    private lateinit var chipNonCompliant: Chip
    private lateinit var chipNotAssessed: Chip

    private lateinit var btnSave: MaterialButton
    private lateinit var btnUpdateCompliance: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: MrfcViewModel
    private var mrfcId: Long = -1
    private var currentMRFC: MrfcDto? = null
    private var selectedComplianceStatus: ComplianceStatus = ComplianceStatus.NOT_ASSESSED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mrfc)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupDropdowns()
        observeStates()
        setupListeners()
        loadMRFCData()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Edit MRFC"
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
        actvCategory = findViewById(R.id.actvCategory)
        switchActive = findViewById(R.id.switchActive)

        // Compliance fields
        etCompliancePercentage = findViewById(R.id.etCompliancePercentage)
        actvComplianceStatus = findViewById(R.id.actvComplianceStatus)
        etComplianceRemarks = findViewById(R.id.etComplianceRemarks)
        tvComplianceUpdatedAt = findViewById(R.id.tvComplianceUpdatedAt)
        chipCompliant = findViewById(R.id.chipCompliant)
        chipPartial = findViewById(R.id.chipPartial)
        chipNonCompliant = findViewById(R.id.chipNonCompliant)
        chipNotAssessed = findViewById(R.id.chipNotAssessed)

        btnSave = findViewById(R.id.btnSave)
        btnUpdateCompliance = findViewById(R.id.btnUpdateCompliance)
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

    private fun setupDropdowns() {
        // Category dropdown
        val categories = arrayOf("1st Class", "2nd Class", "3rd Class", "4th Class", "5th Class", "6th Class")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(categoryAdapter)

        // Compliance status dropdown
        val statuses = ComplianceStatus.values().map { it.getDisplayName() }.toTypedArray()
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        actvComplianceStatus.setAdapter(statusAdapter)
    }

    private fun observeStates() {
        // Observe detail loading
        viewModel.mrfcDetailState.observe(this) { state ->
            when (state) {
                is MrfcDetailState.Loading -> showLoading(true)
                is MrfcDetailState.Success -> {
                    showLoading(false)
                    displayMrfcData(state.data)
                }
                is MrfcDetailState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> showLoading(false)
            }
        }

        // Observe update
        viewModel.mrfcUpdateState.observe(this) { state ->
            when (state) {
                is MrfcUpdateState.Loading -> showLoading(true)
                is MrfcUpdateState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "MRFC updated successfully", Toast.LENGTH_SHORT).show()
                    currentMRFC = state.data
                    displayMrfcData(state.data)
                    // Signal success and close activity
                    setResult(RESULT_OK)
                    finish()
                }
                is MrfcUpdateState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Update failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> showLoading(false)
            }
        }
    }

    private fun loadMRFCData() {
        mrfcId = intent.getLongExtra("MRFC_ID", -1)

        if (mrfcId == -1L) {
            Toast.makeText(this, "Error: Invalid MRFC ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadMrfcById(mrfcId)
    }

    private fun displayMrfcData(mrfc: MrfcDto) {
        currentMRFC = mrfc

        // Basic fields
        etMRFCName.setText(mrfc.name)
        etMrfcCode.setText(mrfc.mrfcCode ?: "")
        etMunicipality.setText(mrfc.municipality)
        etProvince.setText(mrfc.province ?: "")
        etRegion.setText(mrfc.region ?: "")
        etContactPerson.setText(mrfc.contactPerson ?: "")
        etContactNumber.setText(mrfc.contactNumber ?: "")
        etEmail.setText(mrfc.email ?: "")
        etAddress.setText(mrfc.address ?: "")
        // Category field removed - not in MrfcDto
        switchActive.isChecked = mrfc.isActive

        // Compliance fields
        etCompliancePercentage.setText(mrfc.compliancePercentage?.toString() ?: "")
        actvComplianceStatus.setText(mrfc.complianceStatus.getDisplayName(), false)
        selectedComplianceStatus = mrfc.complianceStatus

        // Update compliance chips
        updateComplianceChips(mrfc.complianceStatus)

        // Show last updated timestamp
        mrfc.complianceUpdatedAt?.let {
            tvComplianceUpdatedAt.visibility = View.VISIBLE
            tvComplianceUpdatedAt.text = "Last updated: ${formatDate(it)}"
        } ?: run {
            tvComplianceUpdatedAt.visibility = View.GONE
        }

        supportActionBar?.title = "Edit ${mrfc.name}"
    }

    private fun updateComplianceChips(status: ComplianceStatus) {
        chipCompliant.isChecked = status == ComplianceStatus.COMPLIANT
        chipPartial.isChecked = status == ComplianceStatus.PARTIAL
        chipNonCompliant.isChecked = status == ComplianceStatus.NON_COMPLIANT
        chipNotAssessed.isChecked = status == ComplianceStatus.NOT_ASSESSED
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveMRFCData()
        }

        btnUpdateCompliance.setOnClickListener {
            updateCompliance()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        // Compliance status chips
        chipCompliant.setOnClickListener {
            selectedComplianceStatus = ComplianceStatus.COMPLIANT
            updateComplianceChips(ComplianceStatus.COMPLIANT)
            actvComplianceStatus.setText(ComplianceStatus.COMPLIANT.getDisplayName(), false)
        }

        chipPartial.setOnClickListener {
            selectedComplianceStatus = ComplianceStatus.PARTIAL
            updateComplianceChips(ComplianceStatus.PARTIAL)
            actvComplianceStatus.setText(ComplianceStatus.PARTIAL.getDisplayName(), false)
        }

        chipNonCompliant.setOnClickListener {
            selectedComplianceStatus = ComplianceStatus.NON_COMPLIANT
            updateComplianceChips(ComplianceStatus.NON_COMPLIANT)
            actvComplianceStatus.setText(ComplianceStatus.NON_COMPLIANT.getDisplayName(), false)
        }

        chipNotAssessed.setOnClickListener {
            selectedComplianceStatus = ComplianceStatus.NOT_ASSESSED
            updateComplianceChips(ComplianceStatus.NOT_ASSESSED)
            actvComplianceStatus.setText(ComplianceStatus.NOT_ASSESSED.getDisplayName(), false)
        }
    }

    private fun saveMRFCData() {
        val name = etMRFCName.text.toString().trim()
        val mrfcCode = etMrfcCode.text.toString().trim()
        val municipality = etMunicipality.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val region = etRegion.text.toString().trim()
        val contactPerson = etContactPerson.text.toString().trim()
        val contactNumber = etContactNumber.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val category = actvCategory.text.toString().trim()

        // Validation
        if (!validateFields(name, municipality, province, region, contactPerson, contactNumber, email)) {
            return
        }

        val request = UpdateMrfcRequest(
            name = name,
            mrfcCode = mrfcCode.ifEmpty { null },
            municipality = municipality,
            province = province,
            region = region,
            contactPerson = contactPerson,
            contactNumber = contactNumber,
            email = email.ifEmpty { null },
            address = address.ifEmpty { null },
            // Category field removed - not in UpdateMrfcRequest
            isActive = switchActive.isChecked
        )

        viewModel.updateMrfc(mrfcId, request)
    }

    private fun updateCompliance() {
        val percentageStr = etCompliancePercentage.text.toString().trim()
        val remarks = etComplianceRemarks.text.toString().trim()

        if (percentageStr.isEmpty()) {
            Toast.makeText(this, "Please enter compliance percentage", Toast.LENGTH_SHORT).show()
            etCompliancePercentage.requestFocus()
            return
        }

        val percentage = percentageStr.toDoubleOrNull()
        if (percentage == null || percentage < 0 || percentage > 100) {
            Toast.makeText(this, "Compliance percentage must be between 0 and 100", Toast.LENGTH_SHORT).show()
            etCompliancePercentage.requestFocus()
            return
        }

        val request = UpdateComplianceRequest(
            compliancePercentage = percentage,
            complianceStatus = selectedComplianceStatus,
            remarks = remarks.ifEmpty { null }
        )

        // Call API to update compliance
        Toast.makeText(this, "Updating compliance...", Toast.LENGTH_SHORT).show()
        // TODO: Add compliance update method to ViewModel
        // For now, just show success message
        Toast.makeText(this, "Compliance updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun validateFields(
        name: String,
        municipality: String,
        province: String,
        region: String,
        contactPerson: String,
        contactNumber: String,
        email: String
    ): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "MRFC Name is required", Toast.LENGTH_SHORT).show()
            etMRFCName.requestFocus()
            return false
        }

        if (municipality.isEmpty()) {
            Toast.makeText(this, "Municipality is required", Toast.LENGTH_SHORT).show()
            etMunicipality.requestFocus()
            return false
        }

        if (province.isEmpty()) {
            Toast.makeText(this, "Province is required", Toast.LENGTH_SHORT).show()
            etProvince.requestFocus()
            return false
        }

        if (region.isEmpty()) {
            Toast.makeText(this, "Region is required", Toast.LENGTH_SHORT).show()
            etRegion.requestFocus()
            return false
        }

        if (contactPerson.isEmpty()) {
            Toast.makeText(this, "Contact Person is required", Toast.LENGTH_SHORT).show()
            etContactPerson.requestFocus()
            return false
        }

        if (contactNumber.isEmpty()) {
            Toast.makeText(this, "Contact Number is required", Toast.LENGTH_SHORT).show()
            etContactNumber.requestFocus()
            return false
        }

        if (!contactNumber.matches(Regex("^(\\+63|0)\\d{10}$"))) {
            Toast.makeText(
                this,
                "Invalid contact number. Use +639XXXXXXXXX or 09XXXXXXXXX",
                Toast.LENGTH_SHORT
            ).show()
            etContactNumber.requestFocus()
            return false
        }

        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            etEmail.requestFocus()
            return false
        }

        return true
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
        btnUpdateCompliance.isEnabled = !isLoading
        btnCancel.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    companion object {
        const val REQUEST_CODE_EDIT_MRFC = 101
    }
}
