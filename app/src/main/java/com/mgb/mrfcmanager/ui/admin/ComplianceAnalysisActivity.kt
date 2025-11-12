package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.mgb.mrfcmanager.ui.base.BaseActivity
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
 * Completely rewritten for better state management and UI clarity
 */
class ComplianceAnalysisActivity : BaseActivity() {

    companion object {
        const val EXTRA_DOCUMENT_ID = "document_id"
        const val EXTRA_DOCUMENT_NAME = "document_name"
        const val EXTRA_AUTO_ANALYZE = "auto_analyze"
    }

    private lateinit var viewModel: ComplianceAnalysisViewModel
    private lateinit var sectionsAdapter: ComplianceSectionsAdapter
    private lateinit var nonCompliantAdapter: NonCompliantItemsAdapter

    // Main views
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var layoutProgress: View
    private lateinit var layoutResults: View
    private lateinit var tvProgressMessage: TextView
    private lateinit var tvProgressDetails: TextView
    
    // Result views
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
    private lateinit var btnReanalyze: MaterialButton

    private var documentId: Long = -1
    private var documentName: String = ""
    private var currentAnalysis: ComplianceAnalysisDto? = null
    private var isPollingProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set status bar color
        window.statusBarColor = resources.getColor(R.color.primary, theme)
        
        setContentView(R.layout.activity_compliance_analysis)

        android.util.Log.d("ComplianceAnalysis", "🚀 Activity started")

        // Get intent data
        documentId = intent.getLongExtra(EXTRA_DOCUMENT_ID, -1)
        documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME) ?: "CMVR Document"
        val autoAnalyze = intent.getBooleanExtra(EXTRA_AUTO_ANALYZE, false)

        android.util.Log.d("ComplianceAnalysis", "📥 Document: $documentName (ID: $documentId), Auto: $autoAnalyze")

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
        setupHomeFab() // Enable home button
        observeViewModel()

        // Set document name
        tvDocumentName.text = documentName

        // Load or trigger analysis
        if (autoAnalyze) {
            android.util.Log.d("ComplianceAnalysis", "🔍 Auto-analyzing...")
            showState(State.PROGRESS, "Starting analysis...")
            startProgressPolling()
            viewModel.analyzeCompliance(documentId)
        } else {
            android.util.Log.d("ComplianceAnalysis", "📊 Loading existing analysis...")
            showState(State.PROGRESS, "Loading analysis...")
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
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        layoutProgress = findViewById(R.id.layoutProgress)
        layoutResults = findViewById(R.id.layoutResults)
        tvProgressMessage = findViewById(R.id.tvProgressMessage)
        tvProgressDetails = findViewById(R.id.tvProgressDetails)
        
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
        btnReanalyze = findViewById(R.id.btnReanalyze)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(ComplianceAnalysisApiService::class.java)
        val repository = ComplianceAnalysisRepository(apiService)
        viewModel = ComplianceAnalysisViewModel(repository)
    }

    private fun setupRecyclerViews() {
        sectionsAdapter = ComplianceSectionsAdapter()
        rvComplianceSections.apply {
            layoutManager = LinearLayoutManager(this@ComplianceAnalysisActivity)
            adapter = sectionsAdapter
        }

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

        btnReanalyze.setOnClickListener {
            reanalyzeDocument()
        }

        btnResetAdjustments.setOnClickListener {
            resetAdjustments()
        }

        btnSaveAdjustments.setOnClickListener {
            saveAdjustments()
        }

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
                        // Ignore
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.analysisState.observe(this) { state ->
            when (state) {
                is ComplianceAnalysisState.Loading -> {
                    // Keep progress visible if already polling
                    if (!isPollingProgress) {
                        showState(State.PROGRESS, "Loading...")
                    }
                }
                is ComplianceAnalysisState.Success -> {
                    currentAnalysis = state.data
                    displayAnalysisResults(state.data)
                }
                is ComplianceAnalysisState.Error -> {
                    showState(State.RESULTS, null)
                    showError("Analysis Error: ${state.message}")
                }
                is ComplianceAnalysisState.Idle -> {
                    // Do nothing
                }
            }
        }

        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateAnalysisState.Loading -> {
                    // Show loading
                }
                is UpdateAnalysisState.Success -> {
                    Toast.makeText(this, "Analysis updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetUpdateState()
                }
                is UpdateAnalysisState.Error -> {
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
        // Update status
        tvAnalysisStatus.text = "Analysis Status: ${formatStatus(analysis.analysisStatus)}"

        // Handle different statuses
        when (analysis.analysisStatus) {
            "FAILED" -> {
                showState(State.RESULTS, null)
                tvCompliancePercentage.text = "N/A"
                tvComplianceRating.text = "Pending Manual Review"
                tvComplianceRating.setTextColor(Color.parseColor("#FF9800"))
                tvComplianceDetails.text = "Analysis failed. Manual review required."
                
                if (!analysis.adminNotes.isNullOrEmpty()) {
                    etAdminNotes.setText(analysis.adminNotes)
                }
                
                findViewById<View>(R.id.cardComplianceSections)?.visibility = View.GONE
                findViewById<View>(R.id.cardNonCompliantItems)?.visibility = View.GONE
                return
            }
            "PENDING" -> {
                android.util.Log.d("ComplianceAnalysis", "📊 Status PENDING - showing progress")
                showState(State.PROGRESS, "Analysis in progress...")
                if (!isPollingProgress) {
                    startProgressPolling()
                }
                return
            }
        }
        
        // Show results
        showState(State.RESULTS, null)

        // Display compliance data
        if (analysis.compliancePercentage != null && analysis.complianceRating != null) {
            tvCompliancePercentage.text = String.format(Locale.US, "%.0f%%", analysis.compliancePercentage)
            tvComplianceRating.text = viewModel.getRatingDisplayText(analysis.complianceRating)

            val color = Color.parseColor(viewModel.getRatingColor(analysis.complianceRating))
            tvCompliancePercentage.setTextColor(color)
        }

        if (analysis.applicableItems != null && analysis.compliantItems != null) {
            tvComplianceDetails.text = "${analysis.compliantItems} out of ${analysis.applicableItems} requirements met"
        }

        tvCompliantCount.text = analysis.compliantItems?.toString() ?: "0"
        tvNonCompliantCount.text = analysis.nonCompliantItems?.toString() ?: "0"
        tvNACount.text = analysis.naItems?.toString() ?: "0"

        // Sections
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

        // Admin adjustments
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

    /**
     * Show either PROGRESS or RESULTS state
     */
    private enum class State {
        PROGRESS,
        RESULTS
    }

    private fun showState(state: State, message: String?) {
        runOnUiThread {
            when (state) {
                State.PROGRESS -> {
                    layoutProgress.visibility = View.VISIBLE
                    layoutResults.visibility = View.GONE
                    tvProgressMessage.text = message ?: "Analyzing document..."
                    android.util.Log.d("ComplianceAnalysis", "🔄 Showing PROGRESS: $message")
                }
                State.RESULTS -> {
                    layoutProgress.visibility = View.GONE
                    layoutResults.visibility = View.VISIBLE
                    android.util.Log.d("ComplianceAnalysis", "✅ Showing RESULTS")
                }
            }
        }
    }

    private fun updateProgress(progress: Int, message: String) {
        runOnUiThread {
            tvProgressMessage.text = message
            tvProgressDetails.text = "$progress% complete"
            android.util.Log.d("ComplianceAnalysis", "📊 Progress: $progress% - $message")
        }
    }

    private fun startProgressPolling() {
        if (isPollingProgress) {
            return
        }
        isPollingProgress = true
        
        android.util.Log.d("ComplianceAnalysis", "🔄 Starting polling")
        
        lifecycleScope.launch {
            while (isPollingProgress) {
                try {
                    when (val result = viewModel.getAnalysisProgress(documentId)) {
                        is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                            val progressData = result.data
                            
                            android.util.Log.d("ComplianceAnalysis", "📊 Progress: ${progressData.status}, ${progressData.progress}%")
                            
                            if (progressData.isNotFound()) {
                                android.util.Log.d("ComplianceAnalysis", "✅ Already cached, stopping polling")
                                isPollingProgress = false
                                showState(State.RESULTS, null)
                                viewModel.getComplianceAnalysis(documentId)
                            } else if (progressData.isCompleted()) {
                                android.util.Log.d("ComplianceAnalysis", "✅ Completed, stopping polling")
                                isPollingProgress = false
                                showState(State.RESULTS, null)
                                viewModel.getComplianceAnalysis(documentId)
                            } else if (progressData.isFailed()) {
                                android.util.Log.e("ComplianceAnalysis", "❌ Failed: ${progressData.error}")
                                isPollingProgress = false
                                showState(State.RESULTS, null)
                                showError(progressData.error ?: "Analysis failed")
                                viewModel.getComplianceAnalysis(documentId)
                            } else {
                                // Update progress
                                updateProgress(progressData.progress, progressData.getDisplayMessage())
                            }
                        }
                        is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                            android.util.Log.w("ComplianceAnalysis", "⚠️ Progress endpoint error")
                        }
                        is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                            // Continue
                        }
                    }
                    
                    if (isPollingProgress) {
                        kotlinx.coroutines.delay(2000)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ComplianceAnalysis", "❌ Polling error: ${e.message}")
                    kotlinx.coroutines.delay(2000)
                }
            }
            
            android.util.Log.d("ComplianceAnalysis", "🛑 Polling stopped")
        }
    }

    private fun resetAdjustments() {
        etManualPercentage.text?.clear()
        spinnerManualRating.text?.clear()
        etAdminNotes.text?.clear()

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

    private fun downloadAndOpenPdf() {
        Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                val pdfFile = downloadPdfFromBackend(documentId, documentName)
                
                val pdfUri = androidx.core.content.FileProvider.getUriForFile(
                    this@ComplianceAnalysisActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(pdfUri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                
                val chooser = Intent.createChooser(intent, "Open PDF with")
                if (chooser.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                } else {
                    Toast.makeText(
                        this@ComplianceAnalysisActivity,
                        "No PDF viewer app found",
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

    private fun reanalyzeDocument() {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reanalyze Document?")
            .setMessage("This will delete the existing analysis and perform a fresh analysis. This process may take several minutes.")
            .setPositiveButton("Reanalyze") { _, _ ->
                // Trigger reanalysis
                android.util.Log.d("ComplianceAnalysis", "🔄 Starting reanalysis...")
                showState(State.PROGRESS, "Re-analyzing document...")
                startProgressPolling()
                viewModel.reanalyzeCompliance(documentId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun downloadPdfFromBackend(documentId: Long, fileName: String): java.io.File = withContext(Dispatchers.IO) {
        try {
            val cacheDir = java.io.File(cacheDir, "pdfs")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val pdfFile = java.io.File(cacheDir, fileName)
            
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                val baseUrl = ApiConfig.BASE_URL.removeSuffix("/")
                val streamUrl = "$baseUrl/documents/$documentId/stream"
                
                val tokenManager = MRFCManagerApp.getTokenManager()
                val token = tokenManager.getAccessToken()
                
                if (token == null) {
                    throw java.io.IOException("Authentication token not available")
                }
                
                val connection = java.net.URL(streamUrl).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 60000
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

    private fun showError(message: String) {
        val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("DISMISS") { /* Dismiss */ }
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(android.R.color.white))
            .setActionTextColor(getColor(android.R.color.white))
        
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.apply {
            maxLines = 5
            setOnClickListener {
                snackbar.dismiss()
            }
        }
        
        snackbar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("ComplianceAnalysis", "🛑 Activity destroyed, stopping polling")
        isPollingProgress = false
    }
}
