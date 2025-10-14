package com.mgb.mrfcmanager.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.AgendaItem
import com.mgb.mrfcmanager.utils.DemoData
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

    private lateinit var agendaAdapter: AgendaAdapter
    private lateinit var mattersAdapter: MattersArisingAdapter

    private var selectedQuarter: String = ""
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

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
        setupAgendaItems()
        setupMattersArising()
        setupListeners()
    }

    private fun loadQuarterInfo() {
        selectedQuarter = intent.getStringExtra("QUARTER") ?: "1st Quarter 2025"
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

        tvQuarter.text = selectedQuarter
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

        if (meetingDate.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: BACKEND - Save agenda to database
        // For now: Just show success message
        Toast.makeText(this, "Agenda saved successfully", Toast.LENGTH_SHORT).show()
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
            .inflate(R.layout.item_matter_arising, parent, false)
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
