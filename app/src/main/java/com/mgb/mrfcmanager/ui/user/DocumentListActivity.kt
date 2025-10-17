package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Document
import com.mgb.mrfcmanager.utils.DemoData

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

    private var mrfcId: Long = 0
    private var mrfcName: String = ""

    private val allDocuments = mutableListOf<Document>()
    private val displayedDocuments = mutableListOf<Document>()
    private lateinit var documentsAdapter: DocumentsAdapter

    private var currentFileTypeFilter = "All"
    private var currentQuarterFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_list)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupFilters()
        setupSearch()
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

    private fun setupRecyclerView() {
        documentsAdapter = DocumentsAdapter(
            displayedDocuments,
            onDocumentClick = { document -> openDocument(document) },
            onDownloadClick = { document -> downloadDocument(document) }
        )

        // Use GridLayoutManager with column count from resources (1 for phone, 2 for tablet)
        val columnCount = resources.getInteger(R.integer.list_grid_columns)
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
        // TODO: BACKEND - Fetch documents from database for this MRFC
        allDocuments.addAll(DemoData.documentList)
        filterDocuments()
    }

    private fun filterDocuments() {
        displayedDocuments.clear()

        val searchQuery = etSearch.text.toString()
        var filtered: List<Document> = allDocuments

        // Filter by file type
        if (currentFileTypeFilter != "All") {
            filtered = filtered.filter { it.fileType == currentFileTypeFilter }
        }

        // Filter by quarter
        if (currentQuarterFilter != "All") {
            filtered = filtered.filter { it.quarter == currentQuarterFilter }
        }

        // Filter by search query
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.fileName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
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

    private fun openDocument(document: Document) {
        // TODO: BACKEND - Open/preview document
        Toast.makeText(this, "Opening ${document.fileName}", Toast.LENGTH_SHORT).show()
    }

    private fun downloadDocument(document: Document) {
        // TODO: BACKEND - Download document to device
        Toast.makeText(this, "Downloading ${document.fileName}", Toast.LENGTH_SHORT).show()
    }

    // Adapter for documents
    class DocumentsAdapter(
        private val documents: List<Document>,
        private val onDocumentClick: (Document) -> Unit,
        private val onDownloadClick: (Document) -> Unit
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
                document: Document,
                onDocumentClick: (Document) -> Unit,
                onDownloadClick: (Document) -> Unit
            ) {
                tvFileName.text = document.fileName
                tvCategory.text = document.category
                tvFileDate.text = "Uploaded on ${document.uploadDate}"

                // Set icon and color based on file type
                val (iconRes, bgColor) = when (document.fileType.uppercase()) {
                    "PDF" -> R.drawable.ic_file to R.color.status_error
                    "EXCEL" -> R.drawable.ic_file to R.color.status_success
                    "WORD" -> R.drawable.ic_file to R.color.status_info
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
