package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.AgendaItem
import com.mgb.mrfcmanager.data.model.MatterArising
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.utils.DemoData
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.AgendaListState
import com.mgb.mrfcmanager.viewmodel.AgendaViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendaViewActivity : AppCompatActivity() {

    private lateinit var btnSelectQuarter: MaterialButton
    private lateinit var tvMeetingDate: TextView
    private lateinit var tvMeetingLocation: TextView
    private lateinit var rvMattersArising: RecyclerView
    private lateinit var tvNoMattersArising: TextView
    private lateinit var rvAgendaItems: RecyclerView
    private lateinit var btnViewNotes: MaterialButton
    private lateinit var btnViewDocuments: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var selectedQuarter = "2nd Quarter 2025"

    private lateinit var mattersArisingAdapter: MattersArisingAdapter
    private val mattersArising = mutableListOf<MatterArising>()

    private lateinit var agendaAdapter: AgendaViewAdapter
    private val agendaItems = mutableListOf<AgendaItem>()

    private lateinit var viewModel: AgendaViewModel
    private var currentAgendas: List<AgendaDto> = emptyList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_view)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeAgendaState()
        loadAgendaData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$mrfcName Agenda"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        btnSelectQuarter = findViewById(R.id.btnSelectQuarter)
        tvMeetingDate = findViewById(R.id.tvMeetingDate)
        tvMeetingLocation = findViewById(R.id.tvMeetingLocation)
        rvMattersArising = findViewById(R.id.rvMattersArising)
        tvNoMattersArising = findViewById(R.id.tvNoMattersArising)
        rvAgendaItems = findViewById(R.id.rvAgendaItems)
        btnViewNotes = findViewById(R.id.btnViewNotes)
        btnViewDocuments = findViewById(R.id.btnViewDocuments)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val agendaApiService = retrofit.create(AgendaApiService::class.java)
        val agendaRepository = AgendaRepository(agendaApiService)
        val factory = AgendaViewModelFactory(agendaRepository)
        viewModel = ViewModelProvider(this, factory)[AgendaViewModel::class.java]
    }

    private fun observeAgendaState() {
        viewModel.agendaListState.observe(this) { state ->
            when (state) {
                is AgendaListState.Loading -> {
                    showLoading(true)
                }
                is AgendaListState.Success -> {
                    showLoading(false)
                    currentAgendas = state.data
                    populateAgendaFromBackend(state.data)
                }
                is AgendaListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                    // Fallback to empty state
                    showEmptyState()
                }
                is AgendaListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showEmptyState() {
        agendaItems.clear()
        agendaAdapter.notifyDataSetChanged()
        mattersArising.clear()
        mattersArisingAdapter.notifyDataSetChanged()
        rvMattersArising.visibility = View.GONE
        tvNoMattersArising.visibility = View.VISIBLE
        tvMeetingDate.text = "No meeting scheduled"
        tvMeetingLocation.text = "N/A"
    }

    private fun populateAgendaFromBackend(agendas: List<AgendaDto>) {
        // Get the first agenda (there should typically be one per quarter)
        val agenda = agendas.firstOrNull()

        if (agenda == null) {
            showEmptyState()
            Toast.makeText(this, "No agenda found for $selectedQuarter", Toast.LENGTH_SHORT).show()
            return
        }

        // Populate meeting info
        agenda.meetingDate?.let {
            try {
                val date = dateFormat.parse(it)
                tvMeetingDate.text = date?.let { d -> displayDateFormat.format(d) } ?: it
            } catch (e: Exception) {
                tvMeetingDate.text = it
            }
        } ?: run {
            tvMeetingDate.text = "Not scheduled"
        }

        // Note: Location is not in AgendaDto - would need to be added to backend
        tvMeetingLocation.text = "MGB Conference Room"

        // Populate agenda items
        agendaItems.clear()
        agenda.items?.forEach { itemDto ->
            agendaItems.add(
                AgendaItem(
                    id = itemDto.id ?: 0L,
                    title = itemDto.title,
                    description = itemDto.description ?: ""
                )
            )
        }
        agendaAdapter.notifyDataSetChanged()

        // TODO: Matters arising would need a separate API endpoint
        // For now, clear matters arising as they're not in the current AgendaDto
        mattersArising.clear()
        mattersArisingAdapter.notifyDataSetChanged()
        rvMattersArising.visibility = View.GONE
        tvNoMattersArising.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        mattersArisingAdapter = MattersArisingAdapter(mattersArising)
        rvMattersArising.layoutManager = LinearLayoutManager(this)
        rvMattersArising.adapter = mattersArisingAdapter

        agendaAdapter = AgendaViewAdapter(agendaItems)
        rvAgendaItems.layoutManager = LinearLayoutManager(this)
        rvAgendaItems.adapter = agendaAdapter
    }

    private fun setupClickListeners() {
        btnSelectQuarter.setOnClickListener {
            showQuarterSelectionDialog()
        }

        btnViewNotes.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            intent.putExtra("MRFC_NAME", mrfcName)
            intent.putExtra("QUARTER", selectedQuarter)
            startActivity(intent)
        }

        btnViewDocuments.setOnClickListener {
            val intent = Intent(this, DocumentListActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            intent.putExtra("MRFC_NAME", mrfcName)
            startActivity(intent)
        }
    }

    private fun showQuarterSelectionDialog() {
        val quarters = arrayOf(
            "1st Quarter 2025",
            "2nd Quarter 2025",
            "3rd Quarter 2025",
            "4th Quarter 2025"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Quarter")
            .setItems(quarters) { dialog, which ->
                selectedQuarter = quarters[which]
                btnSelectQuarter.text = selectedQuarter
                loadAgendaData()
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAgendaData() {
        if (mrfcId == 0L) {
            showError("MRFC ID is required")
            return
        }

        // Parse quarter and year from selectedQuarter (e.g., "2nd Quarter 2025")
        val (quarter, year) = parseQuarterAndYear(selectedQuarter)

        // Load agenda from backend
        viewModel.loadAgendasByMrfcAndQuarter(mrfcId, quarter, year)
    }

    private fun parseQuarterAndYear(quarterString: String): Pair<String, Int> {
        // Parse strings like "1st Quarter 2025" -> ("Q1", 2025)
        val parts = quarterString.split(" ")
        val year = parts.lastOrNull()?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val quarter = when {
            quarterString.contains("1st", ignoreCase = true) -> "Q1"
            quarterString.contains("2nd", ignoreCase = true) -> "Q2"
            quarterString.contains("3rd", ignoreCase = true) -> "Q3"
            quarterString.contains("4th", ignoreCase = true) -> "Q4"
            else -> "Q1"
        }
        return Pair(quarter, year)
    }

    // Simple adapter for agenda items (read-only)
    class AgendaViewAdapter(
        private val items: List<AgendaItem>
    ) : RecyclerView.Adapter<AgendaViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_agenda_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvAgendaTitle: TextView = itemView.findViewById(R.id.tvAgendaTitle)

            fun bind(agendaItem: AgendaItem) {
                tvAgendaTitle.text = agendaItem.title
            }
        }
    }

    // Adapter for matters arising items
    class MattersArisingAdapter(
        private val items: List<MatterArising>
    ) : RecyclerView.Adapter<MattersArisingAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_matter_arising, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvIssue: TextView = itemView.findViewById(R.id.tvIssue)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            private val cardStatus: MaterialCardView = itemView.findViewById(R.id.cardStatus)
            private val tvAssignedTo: TextView = itemView.findViewById(R.id.tvAssignedTo)
            private val tvDateRaised: TextView = itemView.findViewById(R.id.tvDateRaised)
            private val tvRemarks: TextView = itemView.findViewById(R.id.tvRemarks)

            fun bind(matter: MatterArising) {
                tvIssue.text = matter.issue
                tvStatus.text = matter.status
                tvAssignedTo.text = matter.assignedTo
                tvDateRaised.text = matter.dateRaised

                // Show/hide remarks
                if (matter.remarks.isNotEmpty()) {
                    tvRemarks.text = matter.remarks
                    tvRemarks.visibility = View.VISIBLE
                } else {
                    tvRemarks.visibility = View.GONE
                }

                // Set status badge color
                val statusColor = when (matter.status) {
                    "Resolved" -> R.color.status_compliant
                    "In Progress" -> R.color.status_partial
                    "Pending" -> R.color.status_pending
                    else -> R.color.status_pending
                }
                cardStatus.setCardBackgroundColor(itemView.context.getColor(statusColor))
            }
        }
    }
}
