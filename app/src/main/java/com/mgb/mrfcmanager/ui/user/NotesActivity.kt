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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvNotes: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabAddNote: FloatingActionButton

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var quarter: String = ""

    private val allNotes = mutableListOf<Note>()
    private val displayedNotes = mutableListOf<Note>()
    private lateinit var notesAdapter: NotesAdapter

    data class Note(
        val id: Long,
        val title: String,
        val content: String,
        val date: String,
        val quarter: String,
        val mrfcId: Long
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""
        quarter = intent.getStringExtra("QUARTER") ?: ""

        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupSearch()
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

        fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
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
        // TODO: BACKEND - Fetch notes from database
        // Load demo notes
        allNotes.addAll(
            listOf(
                Note(
                    1,
                    "Meeting Notes - Q2 2025",
                    "Discussion on MTF disbursement and AEPEP progress. Action items: Follow up on pending reports and schedule next meeting.",
                    getCurrentDate(),
                    "2nd Quarter 2025",
                    mrfcId
                ),
                Note(
                    2,
                    "CMVR Review Notes",
                    "Reviewed compliance monitoring validation report. All proponents are in compliance. Minor issues noted for follow-up.",
                    "2025-10-10",
                    "2nd Quarter 2025",
                    mrfcId
                ),
                Note(
                    3,
                    "Action Items",
                    "1. Submit quarterly reports by end of month\n2. Schedule site visit for Proponent 3\n3. Update AEPEP financial status",
                    "2025-10-08",
                    "2nd Quarter 2025",
                    mrfcId
                )
            )
        )

        filterNotes("")
    }

    private fun filterNotes(query: String) {
        displayedNotes.clear()

        if (query.isEmpty()) {
            displayedNotes.addAll(allNotes)
        } else {
            displayedNotes.addAll(
                allNotes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
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

    private fun showNoteDetailsDialog(note: Note) {
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

    private fun showNoteMenu(note: Note, view: View) {
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
        // TODO: BACKEND - Save note to database
        val newNote = Note(
            id = System.currentTimeMillis(),
            title = title,
            content = content,
            date = getCurrentDate(),
            quarter = quarter.ifEmpty { "Current Quarter" },
            mrfcId = mrfcId
        )

        allNotes.add(0, newNote)
        filterNotes(etSearch.text.toString())
        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
    }

    private fun updateNote(note: Note, title: String, content: String) {
        // TODO: BACKEND - Update note in database
        val index = allNotes.indexOf(note)
        if (index != -1) {
            allNotes[index] = note.copy(title = title, content = content)
            filterNotes(etSearch.text.toString())
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                // TODO: BACKEND - Delete note from database
                allNotes.remove(note)
                filterNotes(etSearch.text.toString())
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Adapter for notes
    class NotesAdapter(
        private val notes: List<Note>,
        private val onNoteClick: (Note) -> Unit,
        private val onMenuClick: (Note, View) -> Unit
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

            fun bind(note: Note, onNoteClick: (Note) -> Unit, onMenuClick: (Note, View) -> Unit) {
                tvNoteTitle.text = note.title
                tvNoteContent.text = note.content
                tvNoteDate.text = note.date
                tvNoteQuarter.text = note.quarter

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
