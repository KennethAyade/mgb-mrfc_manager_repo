package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.ProponentApiService
import com.mgb.mrfcmanager.data.remote.dto.ProponentDto
import com.mgb.mrfcmanager.data.repository.ProponentRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.ProponentDetailState
import com.mgb.mrfcmanager.viewmodel.ProponentViewModel
import com.mgb.mrfcmanager.viewmodel.ProponentViewModelFactory
import kotlinx.coroutines.launch

class ProponentDetailActivity : AppCompatActivity() {

    private lateinit var tvProponentName: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvPermitNumber: TextView
    private lateinit var tvPermitType: TextView
    private lateinit var tvContactPerson: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ProponentViewModel
    private lateinit var repository: ProponentRepository

    private var proponentId: Long = -1
    private var mrfcId: Long = -1
    private var currentProponent: ProponentDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_detail)

        proponentId = intent.getLongExtra("PROPONENT_ID", -1)
        mrfcId = intent.getLongExtra("MRFC_ID", -1)

        setupToolbar()
        initializeViews()
        setupViewModel()
        observeProponent()
        setupQuarterlyServices()
        loadProponentData()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        tvProponentName = findViewById(R.id.tvProponentName)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvStatus = findViewById(R.id.tvStatus)
        cardStatus = findViewById(R.id.cardStatus)
        tvPermitNumber = findViewById(R.id.tvPermitNumber)
        tvPermitType = findViewById(R.id.tvPermitType)
        tvContactPerson = findViewById(R.id.tvContactPerson)
        tvContactNumber = findViewById(R.id.tvContactNumber)
        tvEmail = findViewById(R.id.tvEmail)
        tvAddress = findViewById(R.id.tvAddress)
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

    private fun observeProponent() {
        viewModel.proponentDetailState.observe(this) { state ->
            when (state) {
                is ProponentDetailState.Loading -> showLoading(true)
                is ProponentDetailState.Success -> {
                    showLoading(false)
                    currentProponent = state.data
                    displayProponentData(state.data)
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

    private fun loadProponentData() {
        if (proponentId == -1L) {
            Toast.makeText(this, "Invalid proponent ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.loadProponentById(proponentId)
    }

    private fun displayProponentData(proponent: ProponentDto) {
        tvProponentName.text = proponent.name
        tvCompanyName.text = proponent.companyName
        tvStatus.text = proponent.status

        // Set status color
        val statusColor = when (proponent.status.uppercase()) {
            "ACTIVE" -> R.color.status_success
            "INACTIVE" -> R.color.status_error
            "SUSPENDED" -> R.color.status_pending
            else -> R.color.status_pending
        }
        cardStatus.setCardBackgroundColor(getColor(statusColor))

        tvPermitNumber.text = proponent.permitNumber ?: "N/A"
        tvPermitType.text = proponent.permitType ?: "N/A"
        tvContactPerson.text = proponent.contactPerson ?: "N/A"
        tvContactNumber.text = proponent.contactNumber ?: "N/A"
        tvEmail.text = proponent.email ?: "N/A"
        tvAddress.text = proponent.address ?: "N/A"

        supportActionBar?.title = proponent.name
    }

    private fun setupQuarterlyServices() {
        var selectedQuarter = 1

        // Quarter selection buttons
        val btnQuarter1 = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnQuarter1)
        val btnQuarter2 = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnQuarter2)
        val btnQuarter3 = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnQuarter3)
        val btnQuarter4 = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnQuarter4)

        val quarterButtons = listOf(btnQuarter1, btnQuarter2, btnQuarter3, btnQuarter4)

        // Set Q1 as default selected
        btnQuarter1.isPressed = true

        // Quarter button click listeners
        quarterButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedQuarter = index + 1
                Toast.makeText(this, "Selected Quarter ${selectedQuarter}", Toast.LENGTH_SHORT).show()
                
                // Update button states (visual feedback)
                quarterButtons.forEach { it.isPressed = false }
                button.isPressed = true
            }
        }

        // Service buttons - Open category-specific document viewers
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFileUpload).setOnClickListener {
            openFileUpload(selectedQuarter)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewNTE).setOnClickListener {
            openDocumentList("NTE_DISBURSEMENT", selectedQuarter)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewAEPEP).setOnClickListener {
            openDocumentList("AEPEP", selectedQuarter)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewOMVR).setOnClickListener {
            openDocumentList("OMVR", selectedQuarter)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewResearch).setOnClickListener {
            openDocumentList("RESEARCH_ACCOMPLISHMENTS", selectedQuarter)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun openFileUpload(quarter: Int) {
        // TODO: Convert quarter number to quarter_id (needs Quarter API)
        val intent = Intent(this, FileUploadActivity::class.java).apply {
            putExtra("PROPONENT_ID", proponentId)
            putExtra("MRFC_ID", mrfcId)
            // putExtra("QUARTER_ID", quarterId) // Will be implemented when Quarter API is ready
        }
        startActivity(intent)
    }

    private fun openDocumentList(category: String, quarter: Int) {
        // TODO: Convert quarter number to quarter_id (needs Quarter API)
        val intent = Intent(this, DocumentListActivity::class.java).apply {
            putExtra(DocumentListActivity.EXTRA_PROPONENT_ID, proponentId)
            // putExtra(DocumentListActivity.EXTRA_QUARTER_ID, quarterId) // Will be implemented when Quarter API is ready
            putExtra(DocumentListActivity.EXTRA_CATEGORY, category)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_proponent_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                openEditForm()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openEditForm() {
        val intent = Intent(this, ProponentFormActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("PROPONENT_ID", proponentId)
        startActivityForResult(intent, ProponentFormActivity.REQUEST_CODE_EDIT)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == ProponentFormActivity.REQUEST_CODE_EDIT) {
            // Reload proponent data after edit
            viewModel.loadProponentById(proponentId)
            setResult(RESULT_OK) // Propagate result to ProponentListActivity
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Proponent")
            .setMessage("Are you sure you want to delete '${currentProponent?.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProponent()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProponent() {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = repository.deleteProponent(proponentId)) {
                is Result.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this@ProponentDetailActivity,
                        "Proponent deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this@ProponentDetailActivity,
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
