package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.MRFC
import com.mgb.mrfcmanager.utils.DemoData

class MRFCSelectionActivity : AppCompatActivity() {

    private lateinit var rvMRFCList: RecyclerView
    private lateinit var mrfcAdapter: MRFCUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrfc_selection)

        setupToolbar()
        setupRecyclerView()
        loadMRFCs()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        rvMRFCList = findViewById(R.id.rvMRFCList)
        rvMRFCList.layoutManager = LinearLayoutManager(this)

        mrfcAdapter = MRFCUserAdapter(emptyList()) { mrfc ->
            onMRFCSelected(mrfc)
        }
        rvMRFCList.adapter = mrfcAdapter
    }

    private fun loadMRFCs() {
        // TODO: BACKEND - Fetch only MRFCs assigned to the current user
        val userMRFCs = DemoData.mrfcList.take(3) // Simulate user having access to first 3 MRFCs
        mrfcAdapter.updateData(userMRFCs)
    }

    private fun onMRFCSelected(mrfc: MRFC) {
        // Navigate to proponent view or MRFC details
        val intent = Intent(this, ProponentViewActivity::class.java)
        intent.putExtra("MRFC_ID", mrfc.id)
        intent.putExtra("MRFC_NAME", mrfc.name)
        startActivity(intent)
    }

    // Adapter for MRFC list (User portal - read-only)
    class MRFCUserAdapter(
        private var mrfcList: List<MRFC>,
        private val onItemClick: (MRFC) -> Unit
    ) : RecyclerView.Adapter<MRFCUserAdapter.ViewHolder>() {

        fun updateData(newList: List<MRFC>) {
            mrfcList = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mrfc_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(mrfcList[position], onItemClick)
        }

        override fun getItemCount() = mrfcList.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMRFCName: TextView = itemView.findViewById(R.id.tvMRFCName)
            private val tvMunicipality: TextView = itemView.findViewById(R.id.tvMunicipality)

            fun bind(mrfc: MRFC, onItemClick: (MRFC) -> Unit) {
                tvMRFCName.text = mrfc.name
                tvMunicipality.text = mrfc.municipality

                itemView.setOnClickListener {
                    onItemClick(mrfc)
                }
            }
        }
    }
}
