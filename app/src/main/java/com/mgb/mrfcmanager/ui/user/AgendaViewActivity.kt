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
import com.mgb.mrfcmanager.ui.base.BaseActivity
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
import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.api.MatterArisingApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.remote.dto.MatterArisingDto
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.data.repository.AgendaItemRepository
import com.mgb.mrfcmanager.data.repository.MatterArisingRepository
import com.mgb.mrfcmanager.utils.DemoData
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendaViewActivity : BaseActivity() {

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

    private lateinit var agendaViewModel: AgendaViewModel
    private lateinit var agendaItemViewModel: AgendaItemViewModel
    private lateinit var matterArisingViewModel: MatterArisingViewModel
    
    private var currentAgendas: List<AgendaDto> = emptyList()
    private var currentAgendaId: Long? = null
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
        
        // Setup Agenda ViewModel
        val agendaApiService = retrofit.create(AgendaApiService::class.java)
        val agendaRepository = AgendaRepository(agendaApiService)
        val agendaFactory = AgendaViewModelFactory(agendaRepository)
        agendaViewModel = ViewModelProvider(this, agendaFactory)[AgendaViewModel::class.java]
        
        // Setup Agenda Item ViewModel
        val agendaItemApiService = retrofit.create(AgendaItemApiService::class.java)
        val agendaItemRepository = AgendaItemRepository(agendaItemApiService)
        val agendaItemFactory = AgendaItemViewModelFactory(agendaItemRepository)
        agendaItemViewModel = ViewModelProvider(this, agendaItemFactory)[AgendaItemViewModel::class.java]
        
        // Setup Matter Arising ViewModel
        val matterArisingApiService = retrofit.create(MatterArisingApiService::class.java)
        val matterArisingRepository = MatterArisingRepository(matterArisingApiService)
        val matterArisingFactory = MatterArisingViewModelFactory(matterArisingRepository)
        matterArisingViewModel = ViewModelProvider(this, matterArisingFactory)[MatterArisingViewModel::class.java]
    }

    private fun observeAgendaState() {
        // Observe main agenda loading
        agendaViewModel.agendaListState.observe(this) { state ->
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
        
        // Observe agenda items loading
        agendaItemViewModel.itemsListState.observe(this) { state ->
            when (state) {
                is ItemsListState.Loading -> {
                    // Already showing loading from main agenda
                }
                is ItemsListState.Success -> {
                    displayAgendaItems(state.data)
                }
                is ItemsListState.Error -> {
                    showError("Error loading agenda items: ${state.message}")
                    agendaItems.clear()
                    agendaAdapter.notifyDataSetChanged()
                }
                is ItemsListState.Idle -> {
                    // Do nothing
                }
            }
        }
        
        // Observe matters arising loading
        matterArisingViewModel.mattersListState.observe(this) { state ->
            when (state) {
                is MattersListState.Loading -> {
                    // Already showing loading from main agenda
                }
                is MattersListState.Success -> {
                    displayMattersArising(state.data)
                }
                is MattersListState.Error -> {
                    showError("Error loading matters arising: ${state.message}")
                    mattersArising.clear()
                    mattersArisingAdapter.notifyDataSetChanged()
                    rvMattersArising.visibility = View.GONE
                    tvNoMattersArising.visibility = View.VISIBLE
                }
                is MattersListState.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
            showToast("No agenda found for $selectedQuarter")
            return
        }

        // Store current agenda ID for loading related data
        currentAgendaId = agenda.id

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

        // Populate location if available
        tvMeetingLocation.text = agenda.location ?: "MGB Conference Room"

        // Load agenda items and matters arising for this agenda
        agendaItemViewModel.loadItemsByAgenda(agenda.id)
        matterArisingViewModel.loadMattersByAgenda(agenda.id)
    }
    
    private fun displayAgendaItems(items: List<AgendaItemDto>) {
        agendaItems.clear()
        
        // Convert DTOs to local model
        items.forEach { dto ->
            agendaItems.add(
                AgendaItem(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description ?: ""
                )
            )
        }
        
        agendaAdapter.notifyDataSetChanged()
    }
    
    private fun displayMattersArising(matters: List<MatterArisingDto>) {
        mattersArising.clear()
        
        if (matters.isEmpty()) {
            rvMattersArising.visibility = View.GONE
            tvNoMattersArising.visibility = View.VISIBLE
        } else {
            rvMattersArising.visibility = View.VISIBLE
            tvNoMattersArising.visibility = View.GONE
            
            // Convert DTOs to local model
            matters.forEach { dto ->
                mattersArising.add(
                    MatterArising(
                        id = dto.id,
                        agendaId = dto.agendaId,
                        issue = dto.issue,
                        status = dto.status,
                        assignedTo = dto.assignedTo ?: "Unassigned",
                        dateRaised = dto.dateRaised,
                        remarks = dto.remarks ?: ""
                    )
                )
            }
            
            mattersArisingAdapter.notifyDataSetChanged()
        }
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
        agendaViewModel.loadAgendasByMrfcAndQuarter(mrfcId, quarter, year)
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
