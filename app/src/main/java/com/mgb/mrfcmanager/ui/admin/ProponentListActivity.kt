package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Proponent
import com.mgb.mrfcmanager.utils.DemoData

class ProponentListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProponentAdapter
    private lateinit var tvMRFCName: TextView

    private var mrfcId: Long = -1
    private var mrfcName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_list)

        loadMRFCInfo()
        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupFAB()
    }

    private fun loadMRFCInfo() {
        mrfcId = intent.getLongExtra("MRFC_ID", -1)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: "MRFC"
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViews() {
        tvMRFCName = findViewById(R.id.tvMRFCName)
        tvMRFCName.text = mrfcName
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvProponentList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // TODO: BACKEND - Filter proponents by MRFC ID from database
        // For now: Load all demo proponents
        val proponents = DemoData.proponentList.filter { it.mrfcId == mrfcId }

        adapter = ProponentAdapter(proponents) { proponent ->
            onProponentClicked(proponent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupFAB() {
        findViewById<FloatingActionButton>(R.id.fabAddProponent).setOnClickListener {
            // TODO: Open Add Proponent dialog/activity
            Toast.makeText(this, "Add Proponent - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProponentClicked(proponent: Proponent) {
        val intent = Intent(this, ProponentDetailActivity::class.java)
        intent.putExtra("PROPONENT_ID", proponent.id)
        intent.putExtra("MRFC_ID", mrfcId)
        startActivity(intent)
    }
}

class ProponentAdapter(
    private val proponentList: List<Proponent>,
    private val onItemClick: (Proponent) -> Unit
) : RecyclerView.Adapter<ProponentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proponent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proponent = proponentList[position]
        holder.bind(proponent, position + 1, onItemClick)
    }

    override fun getItemCount() = proponentList.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val cardProponent: MaterialCardView = itemView.findViewById(R.id.cardProponent)
        private val tvProponentNumber: TextView = itemView.findViewById(R.id.tvProponentNumber)
        private val tvProponentName: TextView = itemView.findViewById(R.id.tvProponentName)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(proponent: Proponent, number: Int, onItemClick: (Proponent) -> Unit) {
            tvProponentNumber.text = number.toString()
            tvProponentName.text = proponent.name
            tvCompanyName.text = proponent.companyName
            tvStatus.text = proponent.status

            // Set status color
            val statusColor = when (proponent.status) {
                "Active" -> R.color.status_compliant
                "Inactive" -> R.color.status_non_compliant
                else -> R.color.status_pending
            }
            tvStatus.setTextColor(itemView.context.getColor(statusColor))

            cardProponent.setOnClickListener { onItemClick(proponent) }
        }
    }
}
