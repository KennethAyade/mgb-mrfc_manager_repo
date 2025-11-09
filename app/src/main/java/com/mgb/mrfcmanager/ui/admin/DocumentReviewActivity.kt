package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.DocumentApiService
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.remote.dto.DocumentStatus
import com.mgb.mrfcmanager.data.remote.dto.UpdateDocumentRequest
import com.mgb.mrfcmanager.data.repository.DocumentRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.DocumentListState
import com.mgb.mrfcmanager.viewmodel.DocumentViewModel
import com.mgb.mrfcmanager.viewmodel.DocumentViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Document Review Activity - Approve/Reject uploaded documents
 * Admins can review pending documents and update their status
 */
class DocumentReviewActivity : AppCompatActivity() {

    private lateinit var rvDocuments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipAccepted: Chip
    private lateinit var chipRejected: Chip

    private lateinit var viewModel: DocumentViewModel
    private val documents = mutableListOf<DocumentDto>()
    private lateinit var adapter: DocumentReviewAdapter

    private var currentFilter: DocumentStatus? = null
    private var mrfcId: Long? = null
    private var proponentId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_review)

        mrfcId = intent.getLongExtra("MRFC_ID", -1L).takeIf { it != -1L }
        proponentId = intent.getLongExtra("PROPONENT_ID", -1L).takeIf { it != -1L }

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupFilterChips()
        observeDocumentList()
        loadDocuments()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Review Documents"
        }
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        rvDocuments = findViewById(R.id.rvDocuments)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        chipAll = findViewById(R.id.chipAll)
        chipPending = findViewById(R.id.chipPending)
        chipAccepted = findViewById(R.id.chipAccepted)
        chipRejected = findViewById(R.id.chipRejected)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val documentApiService = retrofit.create(DocumentApiService::class.java)
        val repository = DocumentRepository(documentApiService)
        val factory = DocumentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DocumentViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = DocumentReviewAdapter(
            documents = documents,
            onApproveClick = { document -> showApproveDialog(document) },
            onRejectClick = { document -> showRejectDialog(document) },
            onDownloadClick = { document -> downloadDocument(document) }
        )
        rvDocuments.layoutManager = LinearLayoutManager(this)
        rvDocuments.adapter = adapter
    }

    private fun setupFilterChips() {
        chipAll.setOnClickListener {
            currentFilter = null
            updateChipSelection(chipAll)
            filterDocuments()
        }

        chipPending.setOnClickListener {
            currentFilter = DocumentStatus.PENDING
            updateChipSelection(chipPending)
            filterDocuments()
        }

        chipAccepted.setOnClickListener {
            currentFilter = DocumentStatus.ACCEPTED
            updateChipSelection(chipAccepted)
            filterDocuments()
        }

        chipRejected.setOnClickListener {
            currentFilter = DocumentStatus.REJECTED
            updateChipSelection(chipRejected)
            filterDocuments()
        }

        // Default to pending
        chipPending.isChecked = true
        currentFilter = DocumentStatus.PENDING
    }

    private fun updateChipSelection(selectedChip: Chip) {
        chipAll.isChecked = selectedChip == chipAll
        chipPending.isChecked = selectedChip == chipPending
        chipAccepted.isChecked = selectedChip == chipAccepted
        chipRejected.isChecked = selectedChip == chipRejected
    }

    private fun observeDocumentList() {
        viewModel.documentListState.observe(this) { state ->
            when (state) {
                is DocumentListState.Loading -> {
                    showLoading(true)
                }
                is DocumentListState.Success -> {
                    showLoading(false)
                    documents.clear()
                    documents.addAll(state.data)
                    filterDocuments()
                }
                is DocumentListState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> showLoading(false)
            }
        }
    }

    private fun loadDocuments() {
        when {
            mrfcId != null -> viewModel.loadDocumentsByMrfc(mrfcId!!)
            proponentId != null -> {
                // Load by proponent - would need to add this method to ViewModel
                Toast.makeText(this, "Proponent document loading not implemented yet", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "No MRFC or Proponent ID provided", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun filterDocuments() {
        val filtered = if (currentFilter == null) {
            documents
        } else {
            documents.filter { it.status == currentFilter }
        }

        adapter.updateDocuments(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun showApproveDialog(document: DocumentDto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Approve Document")
            .setMessage("Are you sure you want to approve \"${document.originalName}\"?")
            .setPositiveButton("Approve") { _, _ ->
                approveDocument(document)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectDialog(document: DocumentDto) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reject_document, null)
        val etReason = dialogView.findViewById<TextInputEditText>(R.id.etRejectionReason)

        MaterialAlertDialogBuilder(this)
            .setTitle("Reject Document")
            .setMessage("Please provide a reason for rejection:")
            .setView(dialogView)
            .setPositiveButton("Reject") { _, _ ->
                val reason = etReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Rejection reason is required", Toast.LENGTH_SHORT).show()
                } else {
                    rejectDocument(document, reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun approveDocument(document: DocumentDto) {
        lifecycleScope.launch {
            val request = UpdateDocumentRequest(
                status = DocumentStatus.ACCEPTED,
                remarks = null
            )

            when (val result = viewModel.updateDocument(document.id, request)) {
                is Result.Success -> {
                    Toast.makeText(this@DocumentReviewActivity, "Document approved", Toast.LENGTH_SHORT).show()
                    loadDocuments() // Refresh list
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@DocumentReviewActivity,
                        "Failed to approve: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun rejectDocument(document: DocumentDto, reason: String) {
        lifecycleScope.launch {
            val request = UpdateDocumentRequest(
                status = DocumentStatus.REJECTED,
                remarks = reason
            )

            when (val result = viewModel.updateDocument(document.id, request)) {
                is Result.Success -> {
                    Toast.makeText(this@DocumentReviewActivity, "Document rejected", Toast.LENGTH_SHORT).show()
                    loadDocuments() // Refresh list
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@DocumentReviewActivity,
                        "Failed to reject: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun downloadDocument(document: DocumentDto) {
        viewModel.downloadDocument(document.id)
        Toast.makeText(this, "Downloading ${document.originalName}...", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvDocuments.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvDocuments.visibility = if (isEmpty) View.GONE else View.VISIBLE

        tvEmpty.text = when (currentFilter) {
            DocumentStatus.PENDING -> "No pending documents"
            DocumentStatus.ACCEPTED -> "No accepted documents"
            DocumentStatus.REJECTED -> "No rejected documents"
            null -> "No documents found"
        }
    }

    /**
     * Adapter for document review list
     */
    class DocumentReviewAdapter(
        private var documents: List<DocumentDto>,
        private val onApproveClick: (DocumentDto) -> Unit,
        private val onRejectClick: (DocumentDto) -> Unit,
        private val onDownloadClick: (DocumentDto) -> Unit
    ) : RecyclerView.Adapter<DocumentReviewAdapter.ViewHolder>() {

        fun updateDocuments(newDocuments: List<DocumentDto>) {
            documents = newDocuments
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document_review, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(documents[position], onApproveClick, onRejectClick, onDownloadClick)
        }

        override fun getItemCount() = documents.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
            private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
            private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
            private val tvUploadDate: TextView = itemView.findViewById(R.id.tvUploadDate)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            private val tvRejectionReason: TextView = itemView.findViewById(R.id.tvRejectionReason)
            private val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
            private val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
            private val btnDownload: MaterialButton = itemView.findViewById(R.id.btnDownload)
            private val btnAnalyzeCompliance: MaterialButton = itemView.findViewById(R.id.btnAnalyzeCompliance)

            fun bind(
                document: DocumentDto,
                onApproveClick: (DocumentDto) -> Unit,
                onRejectClick: (DocumentDto) -> Unit,
                onDownloadClick: (DocumentDto) -> Unit
            ) {
                tvFileName.text = document.originalName
                tvCategory.text = document.category.getDisplayName()
                tvFileSize.text = document.fileSizeDisplay
                tvUploadDate.text = formatDate(document.createdAt)

                // Status chip styling
                tvStatus.text = document.status.getDisplayName()
                tvStatus.setTextColor(document.status.getColor())

                // Description (using remarks field)
                if (document.remarks.isNullOrEmpty()) {
                    tvDescription.visibility = View.GONE
                } else {
                    tvDescription.visibility = View.VISIBLE
                    tvDescription.text = document.remarks
                }

                // Rejection reason
                if (document.isRejected && !document.remarks.isNullOrEmpty()) {
                    tvRejectionReason.visibility = View.VISIBLE
                    tvRejectionReason.text = "Reason: ${document.remarks}"
                } else {
                    tvRejectionReason.visibility = View.GONE
                }

                // Action buttons - only show for pending documents
                if (document.isPending) {
                    btnApprove.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE
                    btnApprove.setOnClickListener { onApproveClick(document) }
                    btnReject.setOnClickListener { onRejectClick(document) }
                } else {
                    btnApprove.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }

                // Analyze Compliance button - only show for CMVR documents
                val isCMVR = document.category == com.mgb.mrfcmanager.data.remote.dto.DocumentCategory.CMVR || 
                            document.originalName.uppercase().contains("CMVR")
                if (isCMVR) {
                    btnAnalyzeCompliance.visibility = View.VISIBLE
                    btnAnalyzeCompliance.setOnClickListener {
                        val context = itemView.context
                        val intent = android.content.Intent(context, ComplianceAnalysisActivity::class.java).apply {
                            putExtra(ComplianceAnalysisActivity.EXTRA_DOCUMENT_ID, document.id)
                            putExtra(ComplianceAnalysisActivity.EXTRA_DOCUMENT_NAME, document.originalName)
                            putExtra(ComplianceAnalysisActivity.EXTRA_AUTO_ANALYZE, true)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    btnAnalyzeCompliance.visibility = View.GONE
                }

                btnDownload.setOnClickListener { onDownloadClick(document) }
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
        }
    }
}
