package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.ApiConfig
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.ComplianceAnalysisApiService
import com.mgb.mrfcmanager.data.remote.dto.*
import com.mgb.mrfcmanager.data.repository.ComplianceAnalysisRepository
import com.mgb.mrfcmanager.viewmodel.ComplianceAnalysisState
import com.mgb.mrfcmanager.viewmodel.ComplianceAnalysisViewModel
import com.mgb.mrfcmanager.viewmodel.UpdateAnalysisState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Activity to display and adjust CMVR compliance analysis results
 */
class ComplianceAnalysisActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DOCUMENT_ID = "document_id"
        const val EXTRA_DOCUMENT_NAME = "document_name"
        const val EXTRA_AUTO_ANALYZE = "auto_analyze"
    }

    private lateinit var viewModel: ComplianceAnalysisViewModel
    private lateinit var sectionsAdapter: ComplianceSectionsAdapter
    private lateinit var nonCompliantAdapter: NonCompliantItemsAdapter

    // Views
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvDocumentName: TextView
    private lateinit var tvAnalysisStatus: TextView
    private lateinit var tvCompliancePercentage: TextView
    private lateinit var tvComplianceRating: TextView
    private lateinit var tvComplianceDetails: TextView
    private lateinit var tvCompliantCount: TextView
    private lateinit var tvNonCompliantCount: TextView
    private lateinit var tvNACount: TextView
    private lateinit var rvComplianceSections: RecyclerView
    private lateinit var rvNonCompliantItems: RecyclerView
    private lateinit var etManualPercentage: TextInputEditText
    private lateinit var spinnerManualRating: AutoCompleteTextView
    private lateinit var etAdminNotes: TextInputEditText
    private lateinit var btnResetAdjustments: MaterialButton
    private lateinit var btnSaveAdjustments: MaterialButton
    private lateinit var btnDownloadPdf: MaterialButton

    private var documentId: Long = -1
    private var documentName: String = ""
    private var currentAnalysis: ComplianceAnalysisDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance_analysis)

        // Get intent data
        documentId = intent.getLongExtra(EXTRA_DOCUMENT_ID, -1)
        documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME) ?: "CMVR Document"
        val autoAnalyze = intent.getBooleanExtra(EXTRA_AUTO_ANALYZE, false)

        if (documentId == -1L) {
            Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerViews()
        setupRatingSpinner()
        setupClickListeners()
        observeViewModel()

        // Display document name
        tvDocumentName.text = documentName

        // Load or trigger analysis
        if (autoAnalyze) {
            viewModel.analyzeCompliance(documentId)
        } else {
            viewModel.getComplianceAnalysis(documentId)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Compliance Analysis"
        }
        
        // Set up back button
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Handle system back button
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun initializeViews() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        progressBar = findViewById(R.id.progressBar)
        tvDocumentName = findViewById(R.id.tvDocumentName)
        tvAnalysisStatus = findViewById(R.id.tvAnalysisStatus)
        tvCompliancePercentage = findViewById(R.id.tvCompliancePercentage)
        tvComplianceRating = findViewById(R.id.tvComplianceRating)
        tvComplianceDetails = findViewById(R.id.tvComplianceDetails)
        tvCompliantCount = findViewById(R.id.tvCompliantCount)
        tvNonCompliantCount = findViewById(R.id.tvNonCompliantCount)
        tvNACount = findViewById(R.id.tvNACount)
        rvComplianceSections = findViewById(R.id.rvComplianceSections)
        rvNonCompliantItems = findViewById(R.id.rvNonCompliantItems)
        etManualPercentage = findViewById(R.id.etManualPercentage)
        spinnerManualRating = findViewById(R.id.spinnerManualRating)
        etAdminNotes = findViewById(R.id.etAdminNotes)
        btnResetAdjustments = findViewById(R.id.btnResetAdjustments)
        btnSaveAdjustments = findViewById(R.id.btnSaveAdjustments)
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(ComplianceAnalysisApiService::class.java)
        val repository = ComplianceAnalysisRepository(apiService)
        viewModel = ComplianceAnalysisViewModel(repository)
    }

    private fun setupRecyclerViews() {
        // Compliance sections
        sectionsAdapter = ComplianceSectionsAdapter()
        rvComplianceSections.apply {
            layoutManager = LinearLayoutManager(this@ComplianceAnalysisActivity)
            adapter = sectionsAdapter
        }

        // Non-compliant items
        nonCompliantAdapter = NonCompliantItemsAdapter()
        rvNonCompliantItems.apply {
            layoutManager = LinearLayoutManager(this@ComplianceAnalysisActivity)
            adapter = nonCompliantAdapter
        }
    }

    private fun setupRatingSpinner() {
        val ratings = arrayOf(
            "Fully Compliant",
            "Partially Compliant",
            "Non-Compliant"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ratings)
        spinnerManualRating.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        btnDownloadPdf.setOnClickListener {
            downloadAndOpenPdf()
        }

        btnResetAdjustments.setOnClickListener {
            resetAdjustments()
        }

        btnSaveAdjustments.setOnClickListener {
            saveAdjustments()
        }

        // Auto-calculate rating when percentage changes
        etManualPercentage.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val percentageText = etManualPercentage.text.toString()
                if (percentageText.isNotEmpty()) {
                    try {
                        val percentage = percentageText.toDouble()
                        if (percentage in 0.0..100.0) {
                            val rating = viewModel.calculateRating(percentage)
                            spinnerManualRating.setText(viewModel.getRatingDisplayText(rating), false)
                        }
                    } catch (e: NumberFormatException) {
                        // Ignore invalid input
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.analysisState.observe(this) { state ->
            when (state) {
                is ComplianceAnalysisState.Loading -> {
                    showLoading(true)
                }
                is ComplianceAnalysisState.Success -> {
                    showLoading(false)
                    currentAnalysis = state.data
                    displayAnalysisResults(state.data)
                }
                is ComplianceAnalysisState.Error -> {
                    showLoading(false)
                    showError("Analysis Error: ${state.message}")
                }
                is ComplianceAnalysisState.Idle -> {
                    showLoading(false)
                }
            }
        }

        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateAnalysisState.Loading -> {
                    showLoading(true)
                }
                is UpdateAnalysisState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Analysis updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetUpdateState()
                }
                is UpdateAnalysisState.Error -> {
                    showLoading(false)
                    showError("Update Error: ${state.message}")
                    viewModel.resetUpdateState()
                }
                is UpdateAnalysisState.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    private fun displayAnalysisResults(analysis: ComplianceAnalysisDto) {
        // Analysis status
        tvAnalysisStatus.text = "Analysis Status: ${formatStatus(analysis.analysisStatus)}"

        // Compliance percentage and rating
        if (analysis.compliancePercentage != null && analysis.complianceRating != null) {
            tvCompliancePercentage.text = String.format(Locale.US, "%.0f%%", analysis.compliancePercentage)
            tvComplianceRating.text = viewModel.getRatingDisplayText(analysis.complianceRating)

            // Set color based on rating
            val color = Color.parseColor(viewModel.getRatingColor(analysis.complianceRating))
            tvCompliancePercentage.setTextColor(color)
        }

        // Compliance details
        if (analysis.applicableItems != null && analysis.compliantItems != null) {
            tvComplianceDetails.text = "${analysis.compliantItems} out of ${analysis.applicableItems} requirements met"
        }

        // Counts
        tvCompliantCount.text = analysis.compliantItems?.toString() ?: "0"
        tvNonCompliantCount.text = analysis.nonCompliantItems?.toString() ?: "0"
        tvNACount.text = analysis.naItems?.toString() ?: "0"

        // Compliance sections
        analysis.complianceDetails?.let { details ->
            val sections = mutableListOf<ComplianceSectionDto>()
            details.eccCompliance?.let { sections.add(it) }
            details.epepCompliance?.let { sections.add(it) }
            details.impactManagement?.let { sections.add(it) }
            details.waterQuality?.let { sections.add(it) }
            details.airQuality?.let { sections.add(it) }
            details.noiseQuality?.let { sections.add(it) }
            details.wasteManagement?.let { sections.add(it) }

            if (sections.isNotEmpty()) {
                sectionsAdapter.submitList(sections)
            }
        }

        // Non-compliant items
        analysis.nonCompliantList?.let { items ->
            if (items.isNotEmpty()) {
                nonCompliantAdapter.submitList(items)
                findViewById<View>(R.id.cardNonCompliantItems).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.cardNonCompliantItems).visibility = View.GONE
            }
        }

        // Admin adjustments (if any)
        if (analysis.adminAdjusted) {
            analysis.compliancePercentage?.let {
                etManualPercentage.setText(String.format(Locale.US, "%.1f", it))
            }
            analysis.complianceRating?.let {
                spinnerManualRating.setText(viewModel.getRatingDisplayText(it), false)
            }
            etAdminNotes.setText(analysis.adminNotes ?: "")
        }
    }

    private fun formatStatus(status: String): String {
        return when (status) {
            "PENDING" -> "Pending"
            "COMPLETED" -> "Completed"
            "FAILED" -> "Failed"
            else -> status
        }
    }

    private fun resetAdjustments() {
        etManualPercentage.text?.clear()
        spinnerManualRating.text?.clear()
        etAdminNotes.text?.clear()

        // Restore original values if available
        currentAnalysis?.let { analysis ->
            if (!analysis.adminAdjusted) {
                analysis.compliancePercentage?.let {
                    etManualPercentage.setText(String.format(Locale.US, "%.1f", it))
                }
                analysis.complianceRating?.let {
                    spinnerManualRating.setText(viewModel.getRatingDisplayText(it), false)
                }
            }
        }
    }

    private fun saveAdjustments() {
        val percentageText = etManualPercentage.text.toString().trim()
        val ratingText = spinnerManualRating.text.toString().trim()
        val notes = etAdminNotes.text.toString().trim()

        // Validate percentage
        val percentage = if (percentageText.isNotEmpty()) {
            try {
                val value = percentageText.toDouble()
                if (value !in 0.0..100.0) {
                    Toast.makeText(this, "Percentage must be between 0 and 100", Toast.LENGTH_SHORT).show()
                    return
                }
                value
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid percentage value", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            currentAnalysis?.compliancePercentage
        }

        // Convert rating display text to enum
        val rating = when (ratingText) {
            "Fully Compliant" -> "FULLY_COMPLIANT"
            "Partially Compliant" -> "PARTIALLY_COMPLIANT"
            "Non-Compliant" -> "NON_COMPLIANT"
            else -> currentAnalysis?.complianceRating
        }

        viewModel.updateComplianceAnalysis(
            documentId,
            percentage,
            rating,
            notes.ifEmpty { null }
        )
    }

    /**
     * Download PDF and open with system PDF viewer
     */
    private fun downloadAndOpenPdf() {
        Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // Download PDF from backend stream endpoint to cache directory
                val pdfFile = downloadPdfFromBackend(documentId, documentName)
                
                // Open PDF using FileProvider
                val pdfUri = androidx.core.content.FileProvider.getUriForFile(
                    this@ComplianceAnalysisActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(pdfUri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                
                // Check if there's an app to handle PDFs
                val chooser = Intent.createChooser(intent, "Open PDF with")
                if (chooser.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                } else {
                    Toast.makeText(
                        this@ComplianceAnalysisActivity,
                        "No PDF viewer app found. Please install one.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: java.io.IOException) {
                Toast.makeText(
                    this@ComplianceAnalysisActivity,
                    "Download failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ComplianceAnalysisActivity,
                    "Failed to open PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Download PDF from backend to cache directory
     */
    private suspend fun downloadPdfFromBackend(documentId: Long, fileName: String): java.io.File = withContext(Dispatchers.IO) {
        try {
            // Create cache directory for PDFs
            val cacheDir = java.io.File(cacheDir, "pdfs")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            // Create file
            val pdfFile = java.io.File(cacheDir, fileName)
            
            // Download if not already cached
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                // Get backend base URL from ApiConfig
                val baseUrl = ApiConfig.BASE_URL.removeSuffix("/")
                val streamUrl = "$baseUrl/documents/$documentId/stream"
                
                // Get auth token
                val tokenManager = MRFCManagerApp.getTokenManager()
                val token = tokenManager.getAccessToken()
                
                if (token == null) {
                    throw java.io.IOException("Authentication token not available")
                }
                
                val connection = java.net.URL(streamUrl).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 60000 // 60 seconds
                connection.readTimeout = 60000
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("User-Agent", "MGB MRFC Manager/1.0")
                
                val responseCode = connection.responseCode
                
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    java.io.BufferedInputStream(connection.inputStream).use { input ->
                        pdfFile.outputStream().use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    connection.disconnect()
                } else {
                    connection.disconnect()
                    val errorMsg = when (responseCode) {
                        401 -> "Unauthorized. Please login again."
                        403 -> "Access forbidden"
                        404 -> "File not found on server"
                        500 -> "Server error"
                        else -> "HTTP error code: $responseCode"
                    }
                    throw java.io.IOException(errorMsg)
                }
            }
            
            // Verify file was downloaded successfully
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                throw java.io.IOException("Download failed: file is empty or doesn't exist")
            }
            
            pdfFile
            
        } catch (e: java.net.SocketTimeoutException) {
            throw java.io.IOException("Download timed out. Please check your internet connection.")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Show error message using Snackbar (better UX, doesn't overlap content)
     */
    private fun showError(message: String) {
        val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("DISMISS") { /* Dismiss snackbar */ }
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(android.R.color.white))
            .setActionTextColor(getColor(android.R.color.white))
        
        // Enable multi-line text and click-to-dismiss
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.apply {
            maxLines = 5
            setOnClickListener {
                snackbar.dismiss()
            }
        }
        
        snackbar.show()
    }
}

