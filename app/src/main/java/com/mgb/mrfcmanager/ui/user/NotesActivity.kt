package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.NotesApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.NotesDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import com.mgb.mrfcmanager.data.repository.NotesRepository
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.NotesListState
import com.mgb.mrfcmanager.viewmodel.NotesViewModel
import com.mgb.mrfcmanager.viewmodel.NotesViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvNotes: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var quarter: String = ""
    private var agendaId: Long? = null

    private val allNotes = mutableListOf<NotesDto>()
    private val displayedNotes = mutableListOf<NotesDto>()
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var viewModel: NotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""
        quarter = intent.getStringExtra("QUARTER") ?: ""
        agendaId = intent.getLongExtra("AGENDA_ID", -1L).takeIf { it != -1L }

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        observeNotesState()
        loadNotes()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        etSearch = findViewById(R.id.etSearch)
        rvNotes = findViewById(R.id.rvNotes)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        fabAddNote = findViewById(R.id.fabAddNote)
        progressBar = findViewById(R.id.progressBar)
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh = findViewById(R.id.swipeRefresh)

        fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val notesApiService = retrofit.create(NotesApiService::class.java)
        val notesRepository = NotesRepository(notesApiService)
        val factory = NotesViewModelFactory(notesRepository)
        viewModel = ViewModelProvider(this, factory)[NotesViewModel::class.java]
    }

    private fun setupSwipeRefresh() {
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.setOnRefreshListener {
        //     loadNotes()
        // }
    }

    private fun observeNotesState() {
        viewModel.notesListState.observe(this) { state ->
            when (state) {
                is NotesListState.Loading -> {
                    showLoading(true)
                }
                is NotesListState.Success -> {
                    showLoading(false)
                    allNotes.clear()
                    allNotes.addAll(state.data)
                    filterNotes(etSearch.text.toString())
                }
                is NotesListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is NotesListState.Idle -> {
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
        notesAdapter = NotesAdapter(
            displayedNotes,
            onNoteClick = { note -> showNoteDetailsDialog(note) },
            onMenuClick = { note, view -> showNoteMenu(note, view) }
        )
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = notesAdapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadNotes() {
        // Load notes from backend
        // Notes can be loaded by agenda (for meeting-specific notes) or by MRFC
        if (agendaId != null && agendaId != 0L) {
            viewModel.loadNotesByAgenda(agendaId!!)
        } else if (mrfcId != 0L) {
            viewModel.loadNotesByMrfc(mrfcId)
        } else {
            showError("Please select a meeting or MRFC to view notes")
            return
        }
    }

    private fun filterNotes(query: String) {
        displayedNotes.clear()

        if (query.isEmpty()) {
            displayedNotes.addAll(allNotes)
        } else {
            displayedNotes.addAll(
                allNotes.filter {
                    it.title?.contains(query, ignoreCase = true) == true ||
                    it.content?.contains(query, ignoreCase = true) == true
                }
            )
        }

        notesAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (displayedNotes.isEmpty()) {
            rvNotes.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvNotes.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null)
        val etNoteTitle = dialogView.findViewById<TextInputEditText>(R.id.etNoteTitle)
        val etNoteContent = dialogView.findViewById<TextInputEditText>(R.id.etNoteContent)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val title = etNoteTitle.text.toString().trim()
            val content = etNoteContent.text.toString().trim()

            if (title.isEmpty()) {
                etNoteTitle.error = "Title is required"
                return@setOnClickListener
            }

            if (content.isEmpty()) {
                etNoteContent.error = "Content is required"
                return@setOnClickListener
            }

            saveNote(title, content)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNoteDetailsDialog(note: NotesDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etNoteTitle = dialogView.findViewById<TextInputEditText>(R.id.etNoteTitle)
        val etNoteContent = dialogView.findViewById<TextInputEditText>(R.id.etNoteContent)

        tvDialogTitle.text = "Edit Note"
        etNoteTitle.setText(note.title)
        etNoteContent.setText(note.content)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val title = etNoteTitle.text.toString().trim()
            val content = etNoteContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Title and content are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateNote(note, title, content)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNoteMenu(note: NotesDto, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_note, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    showNoteDetailsDialog(note)
                    true
                }
                R.id.action_delete -> {
                    deleteNote(note)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun saveNote(title: String, content: String) {
        val request = CreateNoteRequest(
            mrfcId = if (mrfcId != 0L) mrfcId else null,
            quarterId = null,
            agendaId = if (agendaId != 0L) agendaId else null,
            title = title,
            content = content,
            isPinned = false
        )

        lifecycleScope.launch {
            when (val result = viewModel.createNote(request)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    Toast.makeText(this@NotesActivity, "Note saved", Toast.LENGTH_SHORT).show()
                    loadNotes() // Refresh list
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showError("Failed to save note: ${result.message}")
                }
                is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun updateNote(note: NotesDto, title: String, content: String) {
        val request = UpdateNoteRequest(
            title = title,
            content = content
        )

        lifecycleScope.launch {
            when (val result = viewModel.updateNote(note.id, request)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    Toast.makeText(this@NotesActivity, "Note updated", Toast.LENGTH_SHORT).show()
                    loadNotes() // Refresh list
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showError("Failed to update note: ${result.message}")
                }
                is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun deleteNote(note: NotesDto) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    when (val result = viewModel.deleteNote(note.id)) {
                        is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                            Toast.makeText(this@NotesActivity, "Note deleted", Toast.LENGTH_SHORT).show()
                            loadNotes() // Refresh list
                        }
                        is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                            showError("Failed to delete note: ${result.message}")
                        }
                        is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                            // Nothing to do
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Adapter for notes
    class NotesAdapter(
        private val notes: List<NotesDto>,
        private val onNoteClick: (NotesDto) -> Unit,
        private val onMenuClick: (NotesDto, View) -> Unit
    ) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(notes[position], onNoteClick, onMenuClick)
        }

        override fun getItemCount() = notes.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvNoteTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
            private val tvNoteContent: TextView = itemView.findViewById(R.id.tvNoteContent)
            private val tvNoteDate: TextView = itemView.findViewById(R.id.tvNoteDate)
            private val tvNoteQuarter: TextView = itemView.findViewById(R.id.tvNoteQuarter)
            private val ivMenu: ImageView = itemView.findViewById(R.id.ivMenu)

            fun bind(note: NotesDto, onNoteClick: (NotesDto) -> Unit, onMenuClick: (NotesDto, View) -> Unit) {
                tvNoteTitle.text = note.title ?: "Untitled"
                tvNoteContent.text = note.content ?: ""

                // Format date
                val date = try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val parsedDate = format.parse(note.createdAt)
                    parsedDate?.let { displayFormat.format(it) } ?: note.createdAt
                } catch (e: Exception) {
                    note.createdAt
                }
                tvNoteDate.text = date

                // Display note type based on context
                tvNoteQuarter.text = when {
                    note.isPinned -> "📌 Pinned"
                    note.agendaId != null -> "Meeting Note"
                    note.quarterId != null -> "Quarter Note"
                    else -> "General Note"
                }

                itemView.setOnClickListener {
                    onNoteClick(note)
                }

                ivMenu.setOnClickListener {
                    onMenuClick(note, it)
                }
            }
        }
    }
}
