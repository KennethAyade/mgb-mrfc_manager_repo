package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.MRFC
import com.mgb.mrfcmanager.utils.DemoData

/**
 * MRFC List Activity - Displays list of all MRFCs
 * TODO: BACKEND - Replace DemoData with database queries
 */
class MRFCListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MRFCAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrfc_list)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupFAB()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvMRFCList)

        // Use GridLayoutManager with column count from resources (1 for phone, 2 for tablet)
        val columnCount = resources.getInteger(R.integer.list_grid_columns)
        recyclerView.layoutManager = GridLayoutManager(this, columnCount)

        // TODO: BACKEND - Replace with data from ViewModel
        adapter = MRFCAdapter(DemoData.mrfcList) { mrfc ->
            onMRFCClicked(mrfc)
        }
        recyclerView.adapter = adapter
    }

    private fun setupFAB() {
        findViewById<FloatingActionButton>(R.id.fabAddMRFC).setOnClickListener {
            // TODO: Open Add MRFC dialog/activity
            Toast.makeText(this, "Add MRFC - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onMRFCClicked(mrfc: MRFC) {
        // Navigate to MRFC Detail
        val intent = Intent(this, MRFCDetailActivity::class.java)
        intent.putExtra("MRFC_ID", mrfc.id)
        startActivity(intent)
    }
}

/**
 * Adapter for MRFC List
 * TODO: BACKEND - Update to use DiffUtil for better performance
 */
class MRFCAdapter(
    private val mrfcList: List<MRFC>,
    private val onItemClick: (MRFC) -> Unit
) : RecyclerView.Adapter<MRFCAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mrfc, parent, false)
        return ViewHolder(view as MaterialButton)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mrfc = mrfcList[position]
        holder.bind(mrfc, onItemClick)
    }

    override fun getItemCount() = mrfcList.size

    class ViewHolder(private val button: MaterialButton) : RecyclerView.ViewHolder(button) {
        fun bind(mrfc: MRFC, onItemClick: (MRFC) -> Unit) {
            button.text = mrfc.name
            button.setOnClickListener { onItemClick(mrfc) }
        }
    }
}
