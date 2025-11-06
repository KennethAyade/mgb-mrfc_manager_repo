package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.ProponentApiService
import com.mgb.mrfcmanager.data.remote.dto.ProponentDto
import com.mgb.mrfcmanager.data.repository.ProponentRepository
import com.mgb.mrfcmanager.viewmodel.ProponentDetailState
import com.mgb.mrfcmanager.viewmodel.ProponentViewModel
import com.mgb.mrfcmanager.viewmodel.ProponentViewModelFactory

class ProponentViewActivity : AppCompatActivity() {

    private lateinit var tvProponentName: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvStatus: TextView
    private lateinit var tvContactPerson: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var btnViewAgenda: MaterialButton
    private lateinit var btnViewDocuments: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ProponentViewModel

    private var proponentId: Long = 0
    private var mrfcId: Long = 0
    private var mrfcName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_view)

        proponentId = intent.getLongExtra("PROPONENT_ID", 0)
        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupClickListeners()
        observeProponentDetail()
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
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val proponentApiService = retrofit.create(ProponentApiService::class.java)
        val repository = ProponentRepository(proponentApiService)
        val factory = ProponentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProponentViewModel::class.java]
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
        if (proponentId > 0) {
            viewModel.loadProponentById(proponentId)
        } else {
            Toast.makeText(this, "Invalid proponent ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeProponentDetail() {
        viewModel.proponentDetailState.observe(this) { state ->
            when (state) {
                is ProponentDetailState.Loading -> {
                    showLoading(true)
                }
                is ProponentDetailState.Success -> {
                    showLoading(false)
                    displayProponentData(state.data)
                }
                is ProponentDetailState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is ProponentDetailState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnViewAgenda.isEnabled = !isLoading
        btnViewDocuments.isEnabled = !isLoading
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

        // Set contact info
        tvContactPerson.text = proponent.contactPerson ?: "N/A"
        tvContactNumber.text = proponent.contactNumber ?: "N/A"
    }
}
