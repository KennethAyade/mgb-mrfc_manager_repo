package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Document
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.DocumentApiService
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.repository.DocumentRepository
import com.mgb.mrfcmanager.utils.DemoData
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.DocumentListState
import com.mgb.mrfcmanager.viewmodel.DocumentViewModel
import com.mgb.mrfcmanager.viewmodel.DocumentViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class DocumentListActivity : AppCompatActivity() {

    private lateinit var ivFilter: ImageView
    private lateinit var etSearch: TextInputEditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipPDF: Chip
    private lateinit var chipExcel: Chip
    private lateinit var chipWord: Chip
    private lateinit var chipGroupQuarter: ChipGroup
    private lateinit var chipAllQuarters: Chip
    private lateinit var chipQ1: Chip
    private lateinit var chipQ2: Chip
    private lateinit var chipQ3: Chip
    private lateinit var chipQ4: Chip
    private lateinit var chipQ4_2024: Chip
    private lateinit var rvDocuments: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var mrfcId: Long = 0
    private var mrfcName: String = ""

    private val allDocuments = mutableListOf<DocumentDto>()
    private val displayedDocuments = mutableListOf<DocumentDto>()
    private lateinit var documentsAdapter: DocumentsAdapter
    private lateinit var viewModel: DocumentViewModel

    private var currentFileTypeFilter = "All"
    private var currentQuarterFilter = "All"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_list)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupFilters()
        setupSearch()
        setupSwipeRefresh()
        observeDocumentState()
        loadDocuments()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$mrfcName Documents"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        ivFilter = findViewById(R.id.ivFilter)
        etSearch = findViewById(R.id.etSearch)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        chipAll = findViewById(R.id.chipAll)
        chipPDF = findViewById(R.id.chipPDF)
        chipExcel = findViewById(R.id.chipExcel)
        chipWord = findViewById(R.id.chipWord)
        chipGroupQuarter = findViewById(R.id.chipGroupQuarter)
        chipAllQuarters = findViewById(R.id.chipAllQuarters)
        chipQ1 = findViewById(R.id.chipQ1)
        chipQ2 = findViewById(R.id.chipQ2)
        chipQ3 = findViewById(R.id.chipQ3)
        chipQ4 = findViewById(R.id.chipQ4)
        chipQ4_2024 = findViewById(R.id.chipQ4_2024)
        rvDocuments = findViewById(R.id.rvDocuments)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        progressBar = findViewById(R.id.progressBar)
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh = findViewById(R.id.swipeRefresh)

        ivFilter.setOnClickListener {
            // Reset all filters
            currentFileTypeFilter = "All"
            currentQuarterFilter = "All"
            chipAll.isChecked = true
            chipAllQuarters.isChecked = true
            filterDocuments()
            Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val documentApiService = retrofit.create(DocumentApiService::class.java)
        val documentRepository = DocumentRepository(documentApiService)
        val factory = DocumentViewModelFactory(documentRepository)
        viewModel = ViewModelProvider(this, factory)[DocumentViewModel::class.java]
    }

    private fun setupSwipeRefresh() {
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.setOnRefreshListener {
        //     loadDocuments()
        // }
    }

    private fun observeDocumentState() {
        viewModel.documentListState.observe(this) { state ->
            when (state) {
                is DocumentListState.Loading -> {
                    showLoading(true)
                }
                is DocumentListState.Success -> {
                    showLoading(false)
                    allDocuments.clear()
                    allDocuments.addAll(state.data)
                    filterDocuments()
                }
                is DocumentListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is DocumentListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.isRefreshing = false
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupRecyclerView() {
        documentsAdapter = DocumentsAdapter(
            displayedDocuments,
            onDocumentClick = { document -> openDocument(document) },
            onDownloadClick = { document -> downloadDocument(document) }
        )

        // Use GridLayoutManager with column count based on screen width
        // 1 column for phones, 2 columns for tablets
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = if (screenWidthDp >= 600) 2 else 1
        rvDocuments.layoutManager = GridLayoutManager(this, columnCount)
        rvDocuments.adapter = documentsAdapter
    }

    private fun setupFilters() {
        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFileTypeFilter = "All"
                uncheckOtherChips(chipAll)
                filterDocuments()
            }
        }

        chipPDF.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFileTypeFilter = "PDF"
                uncheckOtherChips(chipPDF)
                filterDocuments()
            }
        }

        chipExcel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFileTypeFilter = "Excel"
                uncheckOtherChips(chipExcel)
                filterDocuments()
            }
        }

        chipWord.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFileTypeFilter = "Word"
                uncheckOtherChips(chipWord)
                filterDocuments()
            }
        }

        // Quarter filter chips
        chipAllQuarters.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentQuarterFilter = "All"
                uncheckOtherQuarterChips(chipAllQuarters)
                filterDocuments()
            }
        }

        chipQ1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentQuarterFilter = "1st Quarter 2025"
                uncheckOtherQuarterChips(chipQ1)
                filterDocuments()
            }
        }

        chipQ2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentQuarterFilter = "2nd Quarter 2025"
                uncheckOtherQuarterChips(chipQ2)
                filterDocuments()
            }
        }

        chipQ3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentQuarterFilter = "3rd Quarter 2025"
                uncheckOtherQuarterChips(chipQ3)
                filterDocuments()
            }
        }

        chipQ4.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentQuarterFilter = "4th Quarter 2025"
                uncheckOtherQuarterChips(chipQ4)
                filterDocuments()
            }
        }

        chipQ4_2024.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentQuarterFilter = "4th Quarter 2024"
                uncheckOtherQuarterChips(chipQ4_2024)
                filterDocuments()
            }
        }
    }

    private fun uncheckOtherChips(checkedChip: Chip) {
        val chips = listOf(chipAll, chipPDF, chipExcel, chipWord)
        chips.forEach { chip ->
            if (chip != checkedChip && chip.isChecked) {
                chip.isChecked = false
            }
        }
    }

    private fun uncheckOtherQuarterChips(checkedChip: Chip) {
        val chips = listOf(chipAllQuarters, chipQ1, chipQ2, chipQ3, chipQ4, chipQ4_2024)
        chips.forEach { chip ->
            if (chip != checkedChip && chip.isChecked) {
                chip.isChecked = false
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDocuments()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadDocuments() {
        if (mrfcId == 0L) {
            showError("MRFC ID is required")
            return
        }

        // Load documents from backend for this MRFC
        viewModel.loadDocumentsByMrfc(mrfcId)
    }

    private fun filterDocuments() {
        displayedDocuments.clear()

        val searchQuery = etSearch.text.toString()
        var filtered: List<DocumentDto> = allDocuments

        // Filter by file type
        if (currentFileTypeFilter != "All") {
            filtered = filtered.filter { doc ->
                val mimeType = doc.mimeType.lowercase()
                when (currentFileTypeFilter) {
                    "PDF" -> mimeType.contains("pdf")
                    "Excel" -> mimeType.contains("spreadsheet") || mimeType.contains("excel")
                    "Word" -> mimeType.contains("word") || mimeType.contains("document")
                    else -> true
                }
            }
        }

        // Filter by document type (quarter/category)
        if (currentQuarterFilter != "All") {
            filtered = filtered.filter { doc ->
                doc.documentType.contains(currentQuarterFilter, ignoreCase = true)
            }
        }

        // Filter by search query
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.fileName.contains(searchQuery, ignoreCase = true) ||
                it.documentType.contains(searchQuery, ignoreCase = true) ||
                it.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        displayedDocuments.addAll(filtered)
        documentsAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (displayedDocuments.isEmpty()) {
            rvDocuments.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvDocuments.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun openDocument(document: DocumentDto) {
        // Open document - for now just show message
        // TODO: Implement proper document viewer or open with external app
        Toast.makeText(this, "Opening ${document.fileName}", Toast.LENGTH_SHORT).show()
    }

    private fun downloadDocument(document: DocumentDto) {
        Toast.makeText(this, "Downloading ${document.fileName}...", Toast.LENGTH_SHORT).show()

        // TODO: Implement actual download from backend
        // This would need to:
        // 1. Call viewModel.downloadDocument(document.id)
        // 2. Save ResponseBody to Downloads folder
        // 3. Notify user of completion
        Toast.makeText(this, "Download feature requires backend integration", Toast.LENGTH_SHORT).show()
    }

    // Adapter for documents
    class DocumentsAdapter(
        private val documents: List<DocumentDto>,
        private val onDocumentClick: (DocumentDto) -> Unit,
        private val onDownloadClick: (DocumentDto) -> Unit
    ) : RecyclerView.Adapter<DocumentsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(documents[position], onDocumentClick, onDownloadClick)
        }

        override fun getItemCount() = documents.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardFileType: MaterialCardView = itemView.findViewById(R.id.cardFileType)
            private val ivFileIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
            private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
            private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
            private val tvFileDate: TextView = itemView.findViewById(R.id.tvFileDate)
            private val ivDownload: ImageView = itemView.findViewById(R.id.ivDownload)

            fun bind(
                document: DocumentDto,
                onDocumentClick: (DocumentDto) -> Unit,
                onDownloadClick: (DocumentDto) -> Unit
            ) {
                tvFileName.text = document.fileName
                tvCategory.text = document.documentType

                // Format date
                val uploadDate = try {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val date = format.parse(document.uploadDate)
                    date?.let { displayFormat.format(it) } ?: document.uploadDate
                } catch (e: Exception) {
                    document.uploadDate
                }
                tvFileDate.text = "Uploaded on $uploadDate"

                // Set icon and color based on mime type
                val (iconRes, bgColor) = when {
                    document.mimeType.contains("pdf", ignoreCase = true) ->
                        R.drawable.ic_file to R.color.status_error
                    document.mimeType.contains("spreadsheet", ignoreCase = true) ||
                    document.mimeType.contains("excel", ignoreCase = true) ->
                        R.drawable.ic_file to R.color.status_success
                    document.mimeType.contains("word", ignoreCase = true) ||
                    document.mimeType.contains("document", ignoreCase = true) ->
                        R.drawable.ic_file to R.color.status_info
                    document.mimeType.contains("image", ignoreCase = true) ->
                        R.drawable.ic_file to R.color.status_warning
                    else -> R.drawable.ic_file to R.color.primary
                }

                ivFileIcon.setImageResource(iconRes)
                cardFileType.setCardBackgroundColor(itemView.context.getColor(bgColor))
                ivFileIcon.setColorFilter(itemView.context.getColor(android.R.color.white))

                itemView.setOnClickListener {
                    onDocumentClick(document)
                }

                ivDownload.setOnClickListener {
                    onDownloadClick(document)
                }
            }
        }
    }
}
