package com.mgb.mrfcmanager.ui.meeting.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.repository.AgendaItemRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModelFactory
import com.mgb.mrfcmanager.viewmodel.ItemsListState

/**
 * Agenda Fragment
 * Shows list of agenda items (discussion topics)
 * ALL users can view and add agenda items
 */
class AgendaFragment : Fragment() {

    private lateinit var rvAgendaItems: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: AgendaItemViewModel
    private lateinit var agendaItemAdapter: AgendaItemAdapter

    private val agendaItems = mutableListOf<AgendaItemDto>()
    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"
        private const val ARG_MRFC_ID = "mrfc_id"

        fun newInstance(agendaId: Long, mrfcId: Long): AgendaFragment {
            return AgendaFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_AGENDA_ID, agendaId)
                    putLong(ARG_MRFC_ID, mrfcId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            agendaId = it.getLong(ARG_AGENDA_ID)
            mrfcId = it.getLong(ARG_MRFC_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadAgendaItems()

        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun initializeViews(view: View) {
        rvAgendaItems = view.findViewById(R.id.rvAgendaItems)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(AgendaItemApiService::class.java)
        val repository = AgendaItemRepository(apiService)
        val factory = AgendaItemViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AgendaItemViewModel::class.java]
    }

    private fun setupRecyclerView() {
        agendaItemAdapter = AgendaItemAdapter(agendaItems)
        rvAgendaItems.layoutManager = LinearLayoutManager(requireContext())
        rvAgendaItems.adapter = agendaItemAdapter
    }

    private fun observeViewModel() {
        viewModel.itemsListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ItemsListState.Loading -> {
                    showLoading(true)
                }
                is ItemsListState.Success -> {
                    showLoading(false)
                    updateAgendaItems(state.data)
                }
                is ItemsListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is ItemsListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun loadAgendaItems() {
        viewModel.loadItemsByAgenda(agendaId)
    }

    private fun updateAgendaItems(items: List<AgendaItemDto>) {
        agendaItems.clear()
        agendaItems.addAll(items)
        agendaItemAdapter.notifyDataSetChanged()

        if (agendaItems.isEmpty()) {
            rvAgendaItems.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvAgendaItems.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_agenda_item, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Agenda Item")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    addAgendaItem(title, description)
                } else {
                    Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addAgendaItem(title: String, description: String) {
        val orderIndex = agendaItems.size

        viewModel.createItem(
            agendaId = agendaId,
            title = title,
            description = description,
            orderIndex = orderIndex
        ) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Agenda item added", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    showError("Failed to add item: ${result.message}")
                }
                is Result.Loading -> {
                    // Loading state
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}

/**
 * Adapter for displaying agenda items
 */
class AgendaItemAdapter(
    private val items: List<AgendaItemDto>
) : RecyclerView.Adapter<AgendaItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemNumber: TextView = itemView.findViewById(R.id.tvItemNumber)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)

        fun bind(item: AgendaItemDto, number: Int) {
            tvItemNumber.text = "$number."
            tvItemTitle.text = item.title
            tvItemDescription.text = item.description ?: "No description"
            tvItemDescription.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }
}
