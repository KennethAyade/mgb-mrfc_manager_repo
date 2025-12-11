package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.mgb.mrfcmanager.ui.base.BaseActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.DocumentApiService
import com.mgb.mrfcmanager.data.remote.dto.DocumentCategory
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.repository.DocumentRepository
import com.mgb.mrfcmanager.viewmodel.DocumentListState
import com.mgb.mrfcmanager.viewmodel.DocumentViewModel
import com.mgb.mrfcmanager.viewmodel.DocumentViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * DocumentListActivity - Shows documents filtered by category
 * Accessed from quarterly services buttons in ProponentDetailActivity
 */
class DocumentListActivity : BaseActivity() {

    private lateinit var btnFilterAll: MaterialButton
    private lateinit var btnFilterQ1: MaterialButton
    private lateinit var btnFilterQ2: MaterialButton
    private lateinit var btnFilterQ3: MaterialButton
    private lateinit var btnFilterQ4: MaterialButton
    private lateinit var rvDocuments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var tvCategoryDescription: TextView
    private lateinit var fabUpload: FloatingActionButton
    
    private lateinit var adapter: DocumentListAdapter
    private lateinit var viewModel: DocumentViewModel
    private lateinit var quarterRepository: com.mgb.mrfcmanager.data.repository.QuarterRepository
    private var quarters: List<com.mgb.mrfcmanager.data.remote.dto.QuarterDto> = emptyList()
    
    private var proponentId: Long = -1L
    private var quarterId: Long? = null // Now nullable for "All" filter
    private var category: DocumentCategory? = null
    private var categoryName: String = ""
    
    private val documents = mutableListOf<DocumentDto>()
    private val allDocuments = mutableListOf<DocumentDto>() // Store all documents for filtering

    companion object {
        const val EXTRA_PROPONENT_ID = "PROPONENT_ID"
        const val EXTRA_CATEGORY = "CATEGORY"
        const val REQUEST_CODE_UPLOAD = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_list)

        // Get extras
        proponentId = intent.getLongExtra(EXTRA_PROPONENT_ID, -1L)
        categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
        
        // Convert category name to enum
        category = try {
            DocumentCategory.valueOf(categoryName)
        } catch (_: Exception) {
            null
        }

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupQuarterFilter()
        setupRecyclerView()
        setupFAB()
        setupHomeFab() // Enable home button
        observeDocuments()
        loadQuarters()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
        supportActionBar?.title = category?.getDisplayName() ?: "Documents"
    }

    private fun initializeViews() {
        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterQ1 = findViewById(R.id.btnFilterQ1)
        btnFilterQ2 = findViewById(R.id.btnFilterQ2)
        btnFilterQ3 = findViewById(R.id.btnFilterQ3)
        btnFilterQ4 = findViewById(R.id.btnFilterQ4)
        rvDocuments = findViewById(R.id.rvDocuments)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        tvCategoryDescription = findViewById(R.id.tvCategoryDescription)
        fabUpload = findViewById(R.id.fabUpload)

        // Set category info
        category?.let {
            tvCategoryTitle.text = getString(R.string.category_title_format, it.getIcon(), it.getDisplayName())
            tvCategoryDescription.text = it.getDescription()
        }
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val documentApiService = retrofit.create(DocumentApiService::class.java)
        val documentRepository = DocumentRepository(documentApiService)
        val factory = DocumentViewModelFactory(documentRepository)
        viewModel = ViewModelProvider(this, factory)[DocumentViewModel::class.java]
        
        // Initialize quarter repository
        val quarterApiService = retrofit.create(com.mgb.mrfcmanager.data.remote.api.QuarterApiService::class.java)
        quarterRepository = com.mgb.mrfcmanager.data.repository.QuarterRepository(quarterApiService)
    }
    
    private fun setupQuarterFilter() {
        val filterButtons = listOf(btnFilterAll, btnFilterQ1, btnFilterQ2, btnFilterQ3, btnFilterQ4)

        // Helper function to update filter button states
        fun updateFilterButtonStates(selectedButton: MaterialButton) {
            val selectedColor = getColor(R.color.primary)
            val unselectedColor = getColor(android.R.color.white)
            val selectedTextColor = getColor(android.R.color.white)
            val unselectedTextColor = getColor(R.color.primary) // Changed to primary green for visibility
            
            filterButtons.forEach { button ->
                if (button == selectedButton) {
                    button.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
                    button.setTextColor(selectedTextColor)
                    button.strokeWidth = 0
                } else {
                    button.backgroundTintList = android.content.res.ColorStateList.valueOf(unselectedColor)
                    button.setTextColor(unselectedTextColor)
                    button.strokeWidth = 3 // Make border more visible
                    button.strokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
                }
            }
        }

        // Set "All" as default selected
        updateFilterButtonStates(btnFilterAll)

        // Filter button click listeners
        btnFilterAll.setOnClickListener {
            quarterId = null
            updateFilterButtonStates(btnFilterAll)
            applyQuarterFilter()
        }
        
        btnFilterQ1.setOnClickListener {
            setQuarterFilter(1, btnFilterQ1, ::updateFilterButtonStates)
        }
        
        btnFilterQ2.setOnClickListener {
            setQuarterFilter(2, btnFilterQ2, ::updateFilterButtonStates)
        }
        
        btnFilterQ3.setOnClickListener {
            setQuarterFilter(3, btnFilterQ3, ::updateFilterButtonStates)
        }
        
        btnFilterQ4.setOnClickListener {
            setQuarterFilter(4, btnFilterQ4, ::updateFilterButtonStates)
        }
    }
    
    private fun setQuarterFilter(quarter: Int, button: MaterialButton, updateStates: (MaterialButton) -> Unit) {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val matchingQuarter = quarters.find { 
            it.quarterNumber == quarter && it.year == currentYear 
        }
        
        if (matchingQuarter != null) {
            quarterId = matchingQuarter.id
            updateStates(button)
            applyQuarterFilter()
        } else {
            Toast.makeText(this, "Quarter $quarter not found", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun applyQuarterFilter() {
        documents.clear()
        if (quarterId == null) {
            // Show all documents
            documents.addAll(allDocuments)
        } else {
            // Filter by quarter
            documents.addAll(allDocuments.filter { it.quarterId == quarterId })
        }
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }
    
    private fun loadQuarters() {
        lifecycleScope.launch {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            when (val result = quarterRepository.getQuarters(year = currentYear)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    quarters = result.data
                    loadDocuments() // Load documents after quarters are loaded
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    Toast.makeText(
                        this@DocumentListActivity,
                        "Failed to load quarters: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadDocuments() // Still load documents even if quarters fail
                }
                is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                    // Loading state
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DocumentListAdapter(
            documents = documents,
            onCardClick = { document -> onCardClicked(document) },
            onDownloadClick = { document -> onDownloadClicked(document) }
        )
        // Use GridLayoutManager with column count based on screen width
        // Phone: 1 column, 7" Tablet: 2 columns, 10" Tablet: 3 columns
        val columnCount = resources.getInteger(R.integer.list_grid_columns)
        rvDocuments.layoutManager = GridLayoutManager(this, columnCount)
        rvDocuments.adapter = adapter
    }

    private fun setupFAB() {
        fabUpload.setOnClickListener {
            openFileUpload()
        }
    }

    private fun observeDocuments() {
        viewModel.documentListState.observe(this) { state ->
            when (state) {
                is DocumentListState.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                }
                is DocumentListState.Success -> {
                    showLoading(false)
                    allDocuments.clear()
                    
                    // Filter by category if specified
                    val filteredDocs = if (category != null) {
                        state.data.filter { it.category == category }
                    } else {
                        state.data
                    }
                    
                    allDocuments.addAll(filteredDocs)
                    applyQuarterFilter() // Apply quarter filter on loaded documents
                }
                is DocumentListState.Error -> {
                    showLoading(false)
                    hideEmptyState()
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is DocumentListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun loadDocuments() {
        if (proponentId != -1L) {
            viewModel.loadDocumentsByProponent(proponentId, categoryName.ifEmpty { null })
        } else {
            showEmptyState()
        }
    }

    /**
     * Handle card click:
     * - For CMVR documents: Open Compliance Analysis screen
     * - For other documents: Download and open PDF
     */
    private fun onCardClicked(document: DocumentDto) {
        android.util.Log.d("DocumentList", "====================================")
        android.util.Log.d("DocumentList", "📄 Card clicked for document: ${document.originalName}")
        android.util.Log.d("DocumentList", "Document ID: ${document.id}")
        android.util.Log.d("DocumentList", "Category: ${document.category}")
        android.util.Log.d("DocumentList", "File URL: ${document.fileUrl}")
        
        // Check if CMVR document
        if (document.category == com.mgb.mrfcmanager.data.remote.dto.DocumentCategory.CMVR) {
            android.util.Log.d("DocumentList", "✅ CMVR Document detected - Opening Compliance Analysis")
            android.util.Log.d("DocumentList", "Passing parameters:")
            android.util.Log.d("DocumentList", "  - DOCUMENT_ID: ${document.id}")
            android.util.Log.d("DocumentList", "  - DOCUMENT_NAME: ${document.originalName}")
            android.util.Log.d("DocumentList", "  - AUTO_ANALYZE: false")
            
            // Open Compliance Analysis Activity
            val intent = Intent(this, ComplianceAnalysisActivity::class.java).apply {
                putExtra(ComplianceAnalysisActivity.EXTRA_DOCUMENT_ID, document.id)
                putExtra(ComplianceAnalysisActivity.EXTRA_DOCUMENT_NAME, document.originalName)
                putExtra(ComplianceAnalysisActivity.EXTRA_AUTO_ANALYZE, false) // Changed to false - just view existing analysis
            }
            startActivity(intent)
            android.util.Log.d("DocumentList", "🚀 Navigating to ComplianceAnalysisActivity")
        } else {
            android.util.Log.d("DocumentList", "📥 Non-CMVR Document - Downloading PDF instead")
            // For non-CMVR documents, download and open PDF
            onDownloadClicked(document)
        }
        android.util.Log.d("DocumentList", "====================================")
    }

    /**
     * Handle download button click:
     * Downloads PDF and opens with system PDF viewer
     */
    private fun onDownloadClicked(document: DocumentDto) {
        // Download PDF from backend stream endpoint using FileCacheManager, then open with local file URI
        android.util.Log.d("DocumentList", "Opening document: ${document.originalName}")
        android.util.Log.d("DocumentList", "Document ID: ${document.id}")

        Toast.makeText(this, "Opening PDF...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // Use FileCacheManager for offline-capable caching with LRU eviction
                val fileCacheManager = MRFCManagerApp.getFileCacheManager()
                val tokenManager = MRFCManagerApp.getTokenManager()

                // Check if already cached
                val cachedPath = fileCacheManager.getCachedFilePath(document.id)
                val pdfFile = if (cachedPath != null) {
                    android.util.Log.d("DocumentList", "Using cached PDF: $cachedPath")
                    File(cachedPath)
                } else {
                    // Download and cache
                    Toast.makeText(this@DocumentListActivity, "Downloading PDF...", Toast.LENGTH_SHORT).show()
                    val localPath = fileCacheManager.downloadAndCache(
                        documentId = document.id,
                        fileUrl = document.fileUrl,
                        fileName = document.fileName,
                        originalName = document.originalName,
                        category = document.category.name,
                        authToken = tokenManager.getAccessToken()
                    )

                    if (localPath == null) {
                        throw IllegalStateException("Failed to download file")
                    }

                    File(localPath)
                }
                
                android.util.Log.d("DocumentList", "Opening PDF: ${pdfFile.absolutePath}")
                
                // Open PDF using FileProvider
                val pdfUri = androidx.core.content.FileProvider.getUriForFile(
                    this@DocumentListActivity,
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
                        this@DocumentListActivity,
                        "No PDF viewer app found. Please install one.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: java.io.IOException) {
                android.util.Log.e("DocumentList", "IO Error opening PDF", e)
                Toast.makeText(
                    this@DocumentListActivity,
                    "Download failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IllegalStateException) {
                android.util.Log.e("DocumentList", "Invalid state", e)
                Toast.makeText(
                    this@DocumentListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                android.util.Log.e("DocumentList", "Unexpected error opening PDF", e)
                Toast.makeText(
                    this@DocumentListActivity,
                    "Failed to open PDF: ${e.javaClass.simpleName} - ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openFileUpload() {
        val intent = Intent(this, FileUploadActivity::class.java).apply {
            putExtra("PROPONENT_ID", proponentId)
            putExtra("QUARTER_ID", quarterId)
            putExtra("DEFAULT_CATEGORY", categoryName)
        }
        startActivityForResult(intent, REQUEST_CODE_UPLOAD)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPLOAD && resultCode == RESULT_OK) {
            // Refresh document list
            loadDocuments()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvDocuments.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState() {
        tvEmptyState.visibility = View.VISIBLE
        rvDocuments.visibility = View.GONE
        
        val categoryText = category?.getDisplayName() ?: getString(R.string.this_category)
        tvEmptyState.text = getString(R.string.empty_documents_message, categoryText)
    }

    private fun hideEmptyState() {
        tvEmptyState.visibility = View.GONE
        rvDocuments.visibility = View.VISIBLE
    }
    
    private fun updateEmptyState() {
        if (documents.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }

    // Adapter for document list
    class DocumentListAdapter(
        private val documents: List<DocumentDto>,
        private val onCardClick: (DocumentDto) -> Unit,
        private val onDownloadClick: (DocumentDto) -> Unit
    ) : RecyclerView.Adapter<DocumentListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(documents[position], onCardClick, onDownloadClick)
        }

        override fun getItemCount() = documents.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardDocument: MaterialCardView = itemView.findViewById(R.id.cardDocument)
            private val ivFileIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
            private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
            private val tvFileCategory: TextView = itemView.findViewById(R.id.tvFileCategory)
            private val tvFileDate: TextView = itemView.findViewById(R.id.tvFileDate)
            private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            private val btnDownload: MaterialButton = itemView.findViewById(R.id.btnDownload)

            fun bind(
                document: DocumentDto, 
                onCardClick: (DocumentDto) -> Unit,
                onDownloadClick: (DocumentDto) -> Unit
            ) {
                // File name
                tvFileName.text = document.originalName

                // Category with icon
                val context = itemView.context
                tvFileCategory.text = context.getString(R.string.category_title_format, 
                    document.category.getIcon(), document.category.getDisplayName())

                // Format date
                val uploadDate = try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val date = format.parse(document.createdAt)
                    date?.let { displayFormat.format(it) } ?: document.createdAt
                } catch (_: Exception) {
                    document.createdAt
                }
                tvFileDate.text = uploadDate

                // File size
                tvFileSize.text = document.fileSizeDisplay

                // Status
                tvStatus.text = document.status.getDisplayName()
                tvStatus.setTextColor(document.status.getColor())

                // File type icon
                val iconRes = when {
                    document.originalName.endsWith(".pdf", ignoreCase = true) -> R.drawable.ic_file
                    else -> R.drawable.ic_file
                }
                ivFileIcon.setImageResource(iconRes)

                // Click listeners
                cardDocument.setOnClickListener { onCardClick(document) }
                btnDownload.setOnClickListener { onDownloadClick(document) }
            }
        }
    }
}

