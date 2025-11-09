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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * DocumentListActivity - Shows documents filtered by category
 * Accessed from quarterly services buttons in ProponentDetailActivity
 */
class DocumentListActivity : AppCompatActivity() {

    private lateinit var rvDocuments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var tvCategoryDescription: TextView
    private lateinit var fabUpload: FloatingActionButton
    
    private lateinit var adapter: DocumentListAdapter
    private lateinit var viewModel: DocumentViewModel
    
    private var proponentId: Long = -1L
    private var quarterId: Long = -1L
    private var category: DocumentCategory? = null
    private var categoryName: String = ""
    
    private val documents = mutableListOf<DocumentDto>()

    companion object {
        const val EXTRA_PROPONENT_ID = "PROPONENT_ID"
        const val EXTRA_QUARTER_ID = "QUARTER_ID"
        const val EXTRA_CATEGORY = "CATEGORY"
        const val REQUEST_CODE_UPLOAD = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_list)

        // Get extras
        proponentId = intent.getLongExtra(EXTRA_PROPONENT_ID, -1L)
        quarterId = intent.getLongExtra(EXTRA_QUARTER_ID, -1L)
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
        setupRecyclerView()
        setupFAB()
        observeDocuments()
        loadDocuments()
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
                    documents.clear()
                    
                    // Filter by category if specified
                    val filteredDocs = if (category != null) {
                        state.data.filter { it.category == category }
                    } else {
                        state.data
                    }
                    
                    val oldSize = documents.size
                    documents.addAll(filteredDocs)
                    if (oldSize == 0) {
                        adapter.notifyItemRangeInserted(0, documents.size)
                    } else {
                        adapter.notifyDataSetChanged()
                    }

                    if (documents.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                    }
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
        // Check if CMVR document
        if (document.category == com.mgb.mrfcmanager.data.remote.dto.DocumentCategory.CMVR) {
            // Open Compliance Analysis Activity
            val intent = Intent(this, ComplianceAnalysisActivity::class.java).apply {
                putExtra(ComplianceAnalysisActivity.EXTRA_DOCUMENT_ID, document.id)
                putExtra(ComplianceAnalysisActivity.EXTRA_DOCUMENT_NAME, document.originalName)
                putExtra(ComplianceAnalysisActivity.EXTRA_AUTO_ANALYZE, true)
            }
            startActivity(intent)
        } else {
            // For non-CMVR documents, download and open PDF
            onDownloadClicked(document)
        }
    }

    /**
     * Handle download button click:
     * Downloads PDF and opens with system PDF viewer
     */
    private fun onDownloadClicked(document: DocumentDto) {
        // Download PDF from backend stream endpoint, then open with local file URI
        android.util.Log.d("DocumentList", "Opening document: ${document.originalName}")
        android.util.Log.d("DocumentList", "Document ID: ${document.id}")
        
        Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // Download PDF from backend stream endpoint to cache directory
                val pdfFile = downloadPdfFromBackend(document.id, document.originalName)
                
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
                val baseUrl = com.mgb.mrfcmanager.data.remote.ApiConfig.BASE_URL.removeSuffix("/")
                val streamUrl = "$baseUrl/documents/$documentId/stream"
                
                android.util.Log.d("DocumentList", "Downloading PDF from backend: $streamUrl")
                
                // Get auth token
                val tokenManager = MRFCManagerApp.getTokenManager()
                val token = tokenManager.getAccessToken()
                
                if (token == null) {
                    throw java.io.IOException("Authentication token not available")
                }
                
                val connection = java.net.URL(streamUrl).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 60000 // 60 seconds (larger files may take time)
                connection.readTimeout = 60000 // 60 seconds
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("User-Agent", "MGB MRFC Manager/1.0")
                
                val responseCode = connection.responseCode
                android.util.Log.d("DocumentList", "HTTP Response Code: $responseCode")
                
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val contentLength = connection.contentLength
                    android.util.Log.d("DocumentList", "Content Length: $contentLength bytes")
                    
                    java.io.BufferedInputStream(connection.inputStream).use { input ->
                        pdfFile.outputStream().use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead: Long = 0
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                            }
                            
                            android.util.Log.d("DocumentList", "Downloaded $totalBytesRead bytes")
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
            } else {
                android.util.Log.d("DocumentList", "Using cached PDF: ${pdfFile.absolutePath}")
            }
            
            // Verify file was downloaded successfully
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                throw java.io.IOException("Download failed: file is empty or doesn't exist")
            }
            
            android.util.Log.d("DocumentList", "PDF ready: ${pdfFile.absolutePath} (${pdfFile.length()} bytes)")
            pdfFile
            
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("DocumentList", "Download timeout", e)
            throw java.io.IOException("Download timed out. Please check your internet connection.")
        } catch (e: Exception) {
            android.util.Log.e("DocumentList", "Download error", e)
            throw e
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

