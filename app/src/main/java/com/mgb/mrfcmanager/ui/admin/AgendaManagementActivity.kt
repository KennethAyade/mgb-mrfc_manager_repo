package com.mgb.mrfcmanager.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.AgendaItem
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaRequest
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.utils.DemoData
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.AgendaDetailState
import com.mgb.mrfcmanager.viewmodel.AgendaViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendaManagementActivity : AppCompatActivity() {

    private lateinit var tvQuarter: TextView
    private lateinit var etMeetingDate: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var rvAgendaItems: RecyclerView
    private lateinit var rvMattersArising: RecyclerView
    private lateinit var btnAddMatter: MaterialButton
    private lateinit var btnSaveAgenda: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var agendaAdapter: AgendaAdapter
    private lateinit var mattersAdapter: MattersArisingAdapter
    private lateinit var viewModel: AgendaViewModel

    private var selectedQuarter: String = ""
    private var mrfcId: Long = 0L
    private var agendaId: Long? = null
    private var currentAgenda: AgendaDto? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Demo data for matters arising
    private val mattersList = mutableListOf(
        MatterArising(1, "Follow up on pending compliance reports", "In Progress"),
        MatterArising(2, "Review MTF disbursement procedures", "Completed"),
        MatterArising(3, "Update AEPEP documentation", "Pending")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_management)

        loadQuarterInfo()
        setupToolbar()
        initializeViews()
        setupViewModel()
        setupAgendaItems()
        setupMattersArising()
        setupListeners()
        observeAgendaState()

        // Load existing agenda if agendaId is provided
        agendaId?.let {
            viewModel.loadAgendaById(it)
        }
    }

    private fun loadQuarterInfo() {
        selectedQuarter = intent.getStringExtra("QUARTER") ?: "1st Quarter 2025"
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)
        agendaId = intent.getLongExtra("AGENDA_ID", -1L).takeIf { it != -1L }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        tvQuarter = findViewById(R.id.tvQuarter)
        etMeetingDate = findViewById(R.id.etMeetingDate)
        etLocation = findViewById(R.id.etLocation)
        rvAgendaItems = findViewById(R.id.rvAgendaItems)
        rvMattersArising = findViewById(R.id.rvMattersArising)
        btnAddMatter = findViewById(R.id.btnAddMatter)
        btnSaveAgenda = findViewById(R.id.btnSaveAgenda)
        progressBar = findViewById(R.id.progressBar)

        tvQuarter.text = selectedQuarter
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
        viewModel.agendaDetailState.observe(this) { state ->
            when (state) {
                is AgendaDetailState.Loading -> {
                    showLoading(true)
                }
                is AgendaDetailState.Success -> {
                    showLoading(false)
                    currentAgenda = state.data
                    populateAgendaData(state.data)
                }
                is AgendaDetailState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is AgendaDetailState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun populateAgendaData(agenda: AgendaDto) {
        // Populate form fields with existing agenda data
        agenda.meetingDate?.let {
            etMeetingDate.setText(formatDateForDisplay(it))
        }

        // Note: Location is not in AgendaDto, so we skip it
        // If you need location, it should be added to the backend model

        // Update button text to indicate editing
        btnSaveAgenda.text = "Update Agenda"

        Toast.makeText(this, "Agenda loaded successfully", Toast.LENGTH_SHORT).show()
    }

    private fun formatDateForDisplay(isoDate: String): String {
        return try {
            val parsedDate = apiDateFormat.parse(isoDate)
            parsedDate?.let { dateFormat.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSaveAgenda.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupAgendaItems() {
        rvAgendaItems.layoutManager = LinearLayoutManager(this)

        // TODO: BACKEND - Load agenda items from database
        // For now: Use standard agenda items from demo data
        agendaAdapter = AgendaAdapter(DemoData.standardAgendaItems)
        rvAgendaItems.adapter = agendaAdapter
    }

    private fun setupMattersArising() {
        rvMattersArising.layoutManager = LinearLayoutManager(this)

        // TODO: BACKEND - Load matters arising from database
        mattersAdapter = MattersArisingAdapter(mattersList)
        rvMattersArising.adapter = mattersAdapter
    }

    private fun setupListeners() {
        // Date picker for meeting date
        etMeetingDate.setOnClickListener {
            showDatePicker()
        }

        // Add matter button
        btnAddMatter.setOnClickListener {
            addNewMatter()
        }

        // Save agenda button
        btnSaveAgenda.setOnClickListener {
            saveAgenda()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                etMeetingDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addNewMatter() {
        // TODO: Open dialog to add new matter
        val newMatter = MatterArising(
            mattersList.size + 1,
            "New matter item ${mattersList.size + 1}",
            "Pending"
        )
        mattersList.add(newMatter)
        mattersAdapter.notifyItemInserted(mattersList.size - 1)

        Toast.makeText(this, "Matter added (demo)", Toast.LENGTH_SHORT).show()
    }

    private fun saveAgenda() {
        val meetingDate = etMeetingDate.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (meetingDate.isEmpty()) {
            Toast.makeText(this, "Please select a meeting date", Toast.LENGTH_SHORT).show()
            return
        }

        if (mrfcId == 0L) {
            Toast.makeText(this, "MRFC ID is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract quarter and year from selectedQuarter (e.g., "1st Quarter 2025")
        val (quarter, year) = parseQuarterAndYear(selectedQuarter)

        // Convert meeting date to API format (yyyy-MM-dd)
        val apiMeetingDate = try {
            val parsedDate = dateFormat.parse(meetingDate)
            parsedDate?.let { apiDateFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }

        if (apiMeetingDate.isEmpty()) {
            Toast.makeText(this, "Invalid meeting date format", Toast.LENGTH_SHORT).show()
            return
        }

        // Get agenda items from the adapter
        val agendaItems = agendaAdapter.getItems().mapIndexed { index, item ->
            AgendaItemDto(
                itemNumber = (index + 1).toString(),
                title = item.title,
                description = item.description,
                proponentId = null,
                durationMinutes = 15, // Default 15 minutes
                status = "PENDING"
            )
        }

        val agendaRequest = CreateAgendaRequest(
            mrfcId = mrfcId,
            quarter = quarter,
            year = year,
            agendaNumber = "AGENDA-$quarter-$year",
            title = "Meeting Agenda - $selectedQuarter",
            description = "Agenda for $selectedQuarter meeting",
            meetingDate = apiMeetingDate,
            status = "DRAFT",
            items = agendaItems
        )

        showLoading(true)

        // Use coroutines to call the repository
        lifecycleScope.launch {
            val result = if (agendaId != null) {
                // Update existing agenda
                viewModel.updateAgenda(agendaId!!, agendaRequest)
            } else {
                // Create new agenda
                viewModel.createAgenda(agendaRequest)
            }

            when (result) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this@AgendaManagementActivity,
                        if (agendaId != null) "Agenda updated successfully" else "Agenda created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    showError(result.message)
                }
                is com.mgb.mrfcmanager.data.repository.Result.Loading -> {
                    // Already showing loading
                }
            }
        }
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
}

// Agenda Item Adapter
class AgendaAdapter(
    private val agendaItems: List<AgendaItem>
) : RecyclerView.Adapter<AgendaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = agendaItems[position]
        holder.bind(item, position + 1)
    }

    override fun getItemCount() = agendaItems.size

    fun getItems(): List<AgendaItem> = agendaItems

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemNumber: TextView = itemView.findViewById(R.id.tvItemNumber)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)

        fun bind(item: AgendaItem, number: Int) {
            tvItemNumber.text = number.toString()
            tvItemTitle.text = item.title
        }
    }
}

// Matters Arising Data Class
data class MatterArising(
    val id: Int,
    val details: String,
    val status: String
)

// Matters Arising Adapter
class MattersArisingAdapter(
    private val matters: List<MatterArising>
) : RecyclerView.Adapter<MattersArisingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matter_arising_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val matter = matters[position]
        holder.bind(matter, position + 1)
    }

    override fun getItemCount() = matters.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvMatterItem: TextView = itemView.findViewById(R.id.tvMatterItem)
        private val tvMatterDetails: TextView = itemView.findViewById(R.id.tvMatterDetails)
        private val tvMatterStatus: TextView = itemView.findViewById(R.id.tvMatterStatus)

        fun bind(matter: MatterArising, number: Int) {
            tvMatterItem.text = number.toString()
            tvMatterDetails.text = matter.details
            tvMatterStatus.text = matter.status

            // Set status color
            val statusColor = when (matter.status) {
                "Completed" -> R.color.status_compliant
                "In Progress" -> R.color.status_partial
                "Pending" -> R.color.status_pending
                else -> R.color.text_secondary
            }
            tvMatterStatus.setTextColor(itemView.context.getColor(statusColor))
        }
    }
}
