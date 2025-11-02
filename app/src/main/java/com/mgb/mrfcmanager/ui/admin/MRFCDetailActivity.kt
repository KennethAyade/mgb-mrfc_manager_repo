package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.dto.UpdateMrfcRequest
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.MrfcDetailState
import com.mgb.mrfcmanager.viewmodel.MrfcUpdateState
import com.mgb.mrfcmanager.viewmodel.MrfcViewModel
import com.mgb.mrfcmanager.viewmodel.MrfcViewModelFactory

/**
 * MRFC Detail Activity - View and edit MRFC details
 * Integrated with backend API
 */
class MRFCDetailActivity : AppCompatActivity() {

    private lateinit var etMRFCName: TextInputEditText
    private lateinit var etMunicipality: TextInputEditText
    private lateinit var etContactPerson: TextInputEditText
    private lateinit var etContactNumber: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnViewProponents: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: MrfcViewModel
    private var mrfcId: Long = -1
    private var currentMRFC: MrfcDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrfc_detail)

        setupToolbar()
        initializeViews()
        setupViewModel()
        observeMrfcDetail()
        observeMrfcUpdate()
        setupListeners()
        loadMRFCData()
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
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val mrfcApiService = retrofit.create(MrfcApiService::class.java)
        val repository = MrfcRepository(mrfcApiService)

        val factory = MrfcViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MrfcViewModel::class.java]
    }

    private fun observeMrfcDetail() {
        viewModel.mrfcDetailState.observe(this) { state ->
            when (state) {
                is MrfcDetailState.Idle -> {
                    showLoading(false)
                }
                is MrfcDetailState.Loading -> {
                    showLoading(true)
                }
                is MrfcDetailState.Success -> {
                    showLoading(false)
                    displayMrfcData(state.data)
                }
                is MrfcDetailState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun observeMrfcUpdate() {
        viewModel.mrfcUpdateState.observe(this) { state ->
            when (state) {
                is MrfcUpdateState.Idle -> {
                    showLoading(false)
                }
                is MrfcUpdateState.Loading -> {
                    showLoading(true)
                }
                is MrfcUpdateState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "MRFC updated successfully", Toast.LENGTH_SHORT).show()
                    // Refresh the data to show updated values
                    currentMRFC = state.data
                    displayMrfcData(state.data)
                }
                is MrfcUpdateState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Update failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
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

        // Load MRFC data from backend
        viewModel.loadMrfcById(mrfcId)
    }

    private fun displayMrfcData(mrfc: MrfcDto) {
        currentMRFC = mrfc

        etMRFCName.setText(mrfc.name)
        etMunicipality.setText(mrfc.municipality)
        etContactPerson.setText(mrfc.contactPerson)
        etContactNumber.setText(mrfc.contactNumber)

        supportActionBar?.title = mrfc.name
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

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, "MRFC Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (municipality.isEmpty()) {
            Toast.makeText(this, "Municipality is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (contactPerson.isEmpty()) {
            Toast.makeText(this, "Contact Person is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (contactNumber.isEmpty()) {
            Toast.makeText(this, "Contact Number is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Create update request with backend field names
        val request = UpdateMrfcRequest(
            name = name,
            municipality = municipality,
            province = currentMRFC?.province,
            region = currentMRFC?.region,
            contactPerson = contactPerson,
            contactNumber = contactNumber,
            email = currentMRFC?.email,
            address = currentMRFC?.address
        )

        // Call ViewModel to update MRFC
        viewModel.updateMrfc(mrfcId, request)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
        btnViewProponents.isEnabled = !isLoading

        // Disable input fields while loading
        etMRFCName.isEnabled = !isLoading
        etMunicipality.isEnabled = !isLoading
        etContactPerson.isEnabled = !isLoading
        etContactNumber.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun navigateToProponents() {
        val intent = Intent(this, ProponentListActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NUMBER", currentMRFC?.name ?: "")
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mrfc_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                navigateToEditMRFC()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToEditMRFC() {
        val intent = Intent(this, EditMRFCActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        startActivityForResult(intent, EditMRFCActivity.REQUEST_CODE_EDIT_MRFC)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EditMRFCActivity.REQUEST_CODE_EDIT_MRFC && resultCode == RESULT_OK) {
            // Refresh the MRFC data after editing
            viewModel.loadMrfcById(mrfcId)
            Toast.makeText(this, "MRFC updated successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
