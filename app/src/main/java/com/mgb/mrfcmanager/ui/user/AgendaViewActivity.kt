package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.AgendaItem
import com.mgb.mrfcmanager.data.model.MatterArising
import com.mgb.mrfcmanager.utils.DemoData

class AgendaViewActivity : AppCompatActivity() {

    private lateinit var btnSelectQuarter: MaterialButton
    private lateinit var tvMeetingDate: TextView
    private lateinit var tvMeetingLocation: TextView
    private lateinit var rvMattersArising: RecyclerView
    private lateinit var tvNoMattersArising: TextView
    private lateinit var rvAgendaItems: RecyclerView
    private lateinit var btnViewNotes: MaterialButton
    private lateinit var btnViewDocuments: MaterialButton

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var selectedQuarter = "2nd Quarter 2025"

    private lateinit var mattersArisingAdapter: MattersArisingAdapter
    private val mattersArising = mutableListOf<MatterArising>()

    private lateinit var agendaAdapter: AgendaViewAdapter
    private val agendaItems = mutableListOf<AgendaItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_view)

        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""

        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
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
        // TODO: BACKEND - Fetch agenda data from database based on MRFC and quarter
        agendaItems.clear()
        agendaItems.addAll(DemoData.standardAgendaItems)
        agendaAdapter.notifyDataSetChanged()

        // Load matters arising
        // TODO: BACKEND - Fetch matters arising from database based on MRFC and quarter
        mattersArising.clear()
        mattersArising.addAll(DemoData.mattersArisingList)
        mattersArisingAdapter.notifyDataSetChanged()

        // Show/hide empty state
        if (mattersArising.isEmpty()) {
            rvMattersArising.visibility = View.GONE
            tvNoMattersArising.visibility = View.VISIBLE
        } else {
            rvMattersArising.visibility = View.VISIBLE
            tvNoMattersArising.visibility = View.GONE
        }

        // Set demo meeting info
        tvMeetingDate.text = "2025-06-15"
        tvMeetingLocation.text = "MGB Conference Room"
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
