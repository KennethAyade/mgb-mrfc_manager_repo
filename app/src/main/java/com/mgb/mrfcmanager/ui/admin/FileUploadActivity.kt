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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Document
import com.mgb.mrfcmanager.utils.DemoData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUploadActivity : AppCompatActivity() {

    private lateinit var tilCategory: TextInputLayout
    private lateinit var actvCategory: AutoCompleteTextView
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
    private var selectedCategory: String = ""

    private val uploadedFiles = mutableListOf<Document>()
    private lateinit var filesAdapter: UploadedFilesAdapter

    private val categories = listOf(
        "MTF Report",
        "AEPEP Physical",
        "AEPEP Financial",
        "Research Report",
        "CMVR Report",
        "Minutes",
        "Other"
    )

    companion object {
        private const val FILE_PICKER_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_upload)

        setupToolbar()
        initializeViews()
        setupCategoryDropdown()
        setupRecyclerView()
        setupClickListeners()
        loadDemoData()
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
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        btnBrowseFile = findViewById(R.id.btnBrowseFile)
        btnUpload = findViewById(R.id.btnUpload)
        cardUploadProgress = findViewById(R.id.cardUploadProgress)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        rvUploadedFiles = findViewById(R.id.rvUploadedFiles)
        tvNoFiles = findViewById(R.id.tvNoFiles)
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)
        actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
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
        btnUpload.isEnabled = selectedFileUri != null && selectedCategory.isNotEmpty()
    }

    private fun uploadFile() {
        if (selectedFileUri == null || selectedCategory.isEmpty()) return

        // Show progress
        cardUploadProgress.visibility = View.VISIBLE
        btnUpload.isEnabled = false

        // Simulate upload progress
        // TODO: BACKEND - Implement actual file upload to server/storage
        simulateUpload()
    }

    private fun simulateUpload() {
        Thread {
            for (i in 0..100 step 10) {
                Thread.sleep(100)
                runOnUiThread {
                    progressBar.progress = i
                    tvProgress.text = "$i%"
                }
            }

            // Add to uploaded files list
            val fileType = getFileType(selectedFileName)
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newDocument = Document(
                id = System.currentTimeMillis(),
                fileName = selectedFileName,
                fileType = fileType,
                category = selectedCategory,
                uploadDate = currentDate
            )

            runOnUiThread {
                uploadedFiles.add(0, newDocument)
                filesAdapter.notifyItemInserted(0)
                rvUploadedFiles.scrollToPosition(0)

                // Reset form
                resetForm()
                Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                updateFileListVisibility()
            }
        }.start()
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
        selectedCategory = ""
        tvSelectedFile.text = "No file selected"
        actvCategory.text = null
        cardUploadProgress.visibility = View.GONE
        progressBar.progress = 0
        tvProgress.text = "0%"
        btnUpload.isEnabled = false
    }

    private fun removeFile(document: Document) {
        // TODO: BACKEND - Delete file from server/storage
        val position = uploadedFiles.indexOf(document)
        if (position != -1) {
            uploadedFiles.removeAt(position)
            filesAdapter.notifyItemRemoved(position)
            updateFileListVisibility()
            Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show()
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

    private fun loadDemoData() {
        // Load demo uploaded files
        uploadedFiles.addAll(DemoData.documentList)
        filesAdapter.notifyDataSetChanged()
        updateFileListVisibility()
    }

    // Adapter for uploaded files
    class UploadedFilesAdapter(
        private val files: List<Document>,
        private val onDeleteClick: (Document) -> Unit
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

            fun bind(document: Document, onDeleteClick: (Document) -> Unit) {
                tvFileName.text = document.fileName
                tvFileInfo.text = "${document.category} • ${document.uploadDate}"

                // Set appropriate icon based on file type
                val iconRes = when (document.fileType.uppercase()) {
                    "PDF" -> R.drawable.ic_file
                    "EXCEL" -> R.drawable.ic_file
                    "WORD" -> R.drawable.ic_file
                    else -> R.drawable.ic_file
                }
                ivFileIcon.setImageResource(iconRes)

                ivDelete.setOnClickListener {
                    onDeleteClick(document)
                }
            }
        }
    }
}
