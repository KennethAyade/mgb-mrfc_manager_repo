package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
        } catch (e: Exception) {
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
            tvCategoryTitle.text = "${it.getIcon()} ${it.getDisplayName()}"
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
        adapter = DocumentListAdapter(documents) { document ->
            onDocumentClicked(document)
        }
        rvDocuments.layoutManager = LinearLayoutManager(this)
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
                    
                    documents.addAll(filteredDocs)
                    adapter.notifyDataSetChanged()

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

    private fun onDocumentClicked(document: DocumentDto) {
        // Open document in browser or PDF viewer
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(document.fileUrl)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open document: ${e.message}", Toast.LENGTH_SHORT).show()
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
        
        val categoryText = category?.getDisplayName() ?: "this category"
        tvEmptyState.text = "No $categoryText documents uploaded yet.\n\nTap the + button to upload a document."
    }

    private fun hideEmptyState() {
        tvEmptyState.visibility = View.GONE
        rvDocuments.visibility = View.VISIBLE
    }

    // Adapter for document list
    class DocumentListAdapter(
        private val documents: List<DocumentDto>,
        private val onItemClick: (DocumentDto) -> Unit
    ) : RecyclerView.Adapter<DocumentListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(documents[position], onItemClick)
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

            fun bind(document: DocumentDto, onItemClick: (DocumentDto) -> Unit) {
                // File name
                tvFileName.text = document.originalName

                // Category with icon
                tvFileCategory.text = "${document.category.getIcon()} ${document.category.getDisplayName()}"

                // Format date
                val uploadDate = try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val date = format.parse(document.createdAt)
                    date?.let { displayFormat.format(it) } ?: document.createdAt
                } catch (e: Exception) {
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
                cardDocument.setOnClickListener { onItemClick(document) }
                btnDownload.setOnClickListener { onItemClick(document) }
            }
        }
    }
}

