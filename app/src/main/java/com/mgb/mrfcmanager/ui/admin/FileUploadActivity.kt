package com.mgb.mrfcmanager.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Document
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.DocumentApiService
import com.mgb.mrfcmanager.data.remote.dto.DocumentCategory
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.repository.DocumentRepository
import com.mgb.mrfcmanager.utils.DemoData
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.DocumentListState
import com.mgb.mrfcmanager.viewmodel.DocumentUploadState
import com.mgb.mrfcmanager.viewmodel.DocumentViewModel
import com.mgb.mrfcmanager.viewmodel.DocumentViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUploadActivity : AppCompatActivity() {

    private lateinit var tilCategory: TextInputLayout
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etDescription: TextInputEditText
    private lateinit var tvSelectedFile: TextView
    private lateinit var btnBrowseFile: MaterialButton
    private lateinit var btnUpload: MaterialButton
    private lateinit var cardUploadProgress: MaterialCardView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var rvUploadedFiles: RecyclerView
    private lateinit var tvNoFiles: TextView

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""
    private var selectedCategory: DocumentCategory? = null

    private val uploadedFiles = mutableListOf<DocumentDto>()
    private lateinit var filesAdapter: UploadedFilesAdapter
    private lateinit var viewModel: DocumentViewModel

    private var mrfcId: Long = 0L
    private var proponentId: Long? = null
    private var quarterId: Long? = null

    private val categories = DocumentCategory.values().map { it.getDisplayName() }

    companion object {
        private const val FILE_PICKER_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_upload)

        mrfcId = intent.getLongExtra("MRFC_ID", 0L)
        proponentId = intent.getLongExtra("PROPONENT_ID", -1L).takeIf { it != -1L }
        quarterId = intent.getLongExtra("QUARTER_ID", -1L).takeIf { it != -1L }

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupCategoryDropdown()
        setupRecyclerView()
        setupClickListeners()
        observeUploadState()
        observeDocumentListState()
        loadUploadedFiles()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        tilCategory = findViewById(R.id.tilCategory)
        actvCategory = findViewById(R.id.actvCategory)
        etDescription = findViewById(R.id.etDescription)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        btnBrowseFile = findViewById(R.id.btnBrowseFile)
        btnUpload = findViewById(R.id.btnUpload)
        cardUploadProgress = findViewById(R.id.cardUploadProgress)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        rvUploadedFiles = findViewById(R.id.rvUploadedFiles)
        tvNoFiles = findViewById(R.id.tvNoFiles)
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

    private fun observeUploadState() {
        viewModel.uploadState.observe(this) { state ->
            when (state) {
                is DocumentUploadState.Loading -> {
                    showUploadProgress(true)
                }
                is DocumentUploadState.Success -> {
                    showUploadProgress(false)
                    Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                    resetForm()
                    viewModel.resetUploadState()
                    loadUploadedFiles() // Refresh the list
                }
                is DocumentUploadState.Error -> {
                    showUploadProgress(false)
                    Toast.makeText(this, "Upload failed: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.resetUploadState()
                }
                is DocumentUploadState.Idle -> {
                    showUploadProgress(false)
                }
            }
        }
    }

    private fun observeDocumentListState() {
        viewModel.documentListState.observe(this) { state ->
            when (state) {
                is DocumentListState.Loading -> {
                    // Optional: Show loading indicator for the list
                }
                is DocumentListState.Success -> {
                    uploadedFiles.clear()
                    uploadedFiles.addAll(state.data)
                    filesAdapter.notifyDataSetChanged()
                    updateFileListVisibility()
                }
                is DocumentListState.Error -> {
                    Toast.makeText(this, "Failed to load files: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                is DocumentListState.Idle -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun showUploadProgress(show: Boolean) {
        cardUploadProgress.visibility = if (show) View.VISIBLE else View.GONE
        btnUpload.isEnabled = !show
        btnBrowseFile.isEnabled = !show
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)
        actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = DocumentCategory.values()[position]
            updateUploadButtonState()
        }
    }

    private fun setupRecyclerView() {
        filesAdapter = UploadedFilesAdapter(uploadedFiles) { document ->
            removeFile(document)
        }
        rvUploadedFiles.layoutManager = LinearLayoutManager(this)
        rvUploadedFiles.adapter = filesAdapter
        updateFileListVisibility()
    }

    private fun setupClickListeners() {
        btnBrowseFile.setOnClickListener {
            openFilePicker()
        }

        btnUpload.setOnClickListener {
            uploadFile()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                selectedFileName = getFileName(uri)
                tvSelectedFile.text = selectedFileName
                updateUploadButtonState()
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun updateUploadButtonState() {
        btnUpload.isEnabled = selectedFileUri != null && selectedCategory != null
    }

    private fun uploadFile() {
        if (selectedFileUri == null || selectedCategory == null) {
            Toast.makeText(this, "Please select a file and category", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate that at least one ID is provided (mrfc_id or proponent_id)
        if (mrfcId == 0L && proponentId == null) {
            Toast.makeText(this, "MRFC ID or Proponent ID is required", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = selectedFileUri ?: return
        val category = selectedCategory ?: return

        // Copy file from URI to temp file
        lifecycleScope.launch {
            try {
                val tempFile = createTempFileFromUri(uri, selectedFileName)
                val description = etDescription.text.toString().trim()

                // Upload file using ViewModel
                viewModel.uploadDocument(
                    file = tempFile,
                    category = category,
                    mrfcId = if (mrfcId != 0L) mrfcId else null,
                    proponentId = proponentId,
                    quarterId = quarterId,
                    description = description.ifEmpty { null }
                )

                // Clean up temp file after upload attempt
                tempFile.deleteOnExit()
            } catch (e: Exception) {
                Toast.makeText(
                    this@FileUploadActivity,
                    "Failed to prepare file: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createTempFileFromUri(uri: Uri, fileName: String): File {
        val tempFile = File(cacheDir, fileName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getFileType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
            fileName.endsWith(".xlsx", ignoreCase = true) ||
            fileName.endsWith(".xls", ignoreCase = true) -> "Excel"
            fileName.endsWith(".docx", ignoreCase = true) ||
            fileName.endsWith(".doc", ignoreCase = true) -> "Word"
            else -> "Other"
        }
    }

    private fun resetForm() {
        selectedFileUri = null
        selectedFileName = ""
        selectedCategory = null
        tvSelectedFile.text = "No file selected"
        actvCategory.text = null
        etDescription.text = null
        cardUploadProgress.visibility = View.GONE
        progressBar.progress = 0
        tvProgress.text = "0%"
        btnUpload.isEnabled = false
    }

    private fun removeFile(document: DocumentDto) {
        lifecycleScope.launch {
            val result = viewModel.deleteDocument(document.id)
            when (result) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    val position = uploadedFiles.indexOf(document)
                    if (position != -1) {
                        uploadedFiles.removeAt(position)
                        filesAdapter.notifyItemRemoved(position)
                        updateFileListVisibility()
                        Toast.makeText(this@FileUploadActivity, "File deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    Toast.makeText(
                        this@FileUploadActivity,
                        "Failed to delete: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun updateFileListVisibility() {
        if (uploadedFiles.isEmpty()) {
            rvUploadedFiles.visibility = View.GONE
            tvNoFiles.visibility = View.VISIBLE
        } else {
            rvUploadedFiles.visibility = View.VISIBLE
            tvNoFiles.visibility = View.GONE
        }
    }

    private fun loadUploadedFiles() {
        if (mrfcId == 0L) {
            // If no MRFC ID, just show empty state
            updateFileListVisibility()
            return
        }

        // Load documents from backend for this MRFC
        viewModel.loadDocumentsByMrfc(mrfcId)
    }

    // Adapter for uploaded files
    class UploadedFilesAdapter(
        private val files: List<DocumentDto>,
        private val onDeleteClick: (DocumentDto) -> Unit
    ) : RecyclerView.Adapter<UploadedFilesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_uploaded_file, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(files[position], onDeleteClick)
        }

        override fun getItemCount() = files.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivFileIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
            private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
            private val tvFileInfo: TextView = itemView.findViewById(R.id.tvFileInfo)
            private val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)

            fun bind(document: DocumentDto, onDeleteClick: (DocumentDto) -> Unit) {
                tvFileName.text = document.originalName

                // Format date
                val uploadDate = try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val date = format.parse(document.createdAt)
                    date?.let { displayFormat.format(it) } ?: document.createdAt
                } catch (e: Exception) {
                    document.createdAt
                }

                // Show category, status, and date
                val statusText = when {
                    document.isPending -> "Pending"
                    document.isAccepted -> "Accepted"
                    document.isRejected -> "Rejected"
                    else -> "Unknown"
                }
                tvFileInfo.text = "${document.category.getDisplayName()} • $statusText • $uploadDate"

                // Set appropriate icon based on mime type
                val iconRes = R.drawable.ic_file
                ivFileIcon.setImageResource(iconRes)

                ivDelete.setOnClickListener {
                    onDeleteClick(document)
                }
            }
        }
    }
}
