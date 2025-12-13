package com.mgb.mrfcmanager.ui.meeting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MinutesApiService
import com.mgb.mrfcmanager.data.repository.MinutesRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.MinutesState
import com.mgb.mrfcmanager.viewmodel.MinutesViewModel
import com.mgb.mrfcmanager.viewmodel.MinutesViewModelFactory

/**
 * Minutes Fragment
 * Shows meeting minutes
 * Only the MEETING ORGANIZER can create/edit minutes
 */
class MinutesFragment : Fragment() {

    private lateinit var tvMinutesContent: TextView
    private lateinit var etMinutesSummary: EditText
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: MinutesViewModel

    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L
    private var minutesId: Long? = null
    private var isOrganizer: Boolean = false // TODO: Get from backend

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"
        private const val ARG_MRFC_ID = "mrfc_id"

        fun newInstance(agendaId: Long, mrfcId: Long): MinutesFragment {
            return MinutesFragment().apply {
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

        // Check user role from TokenManager
        // Only ADMIN and SUPER_ADMIN can edit/approve minutes
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        isOrganizer = userRole == "ADMIN" || userRole == "SUPER_ADMIN"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_minutes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        observeViewModel()
        loadMinutes()

        btnEdit.setOnClickListener {
            enableEditMode(true)
        }

        btnSave.setOnClickListener {
            saveMinutes()
        }
    }

    private fun initializeViews(view: View) {
        tvMinutesContent = view.findViewById(R.id.tvMinutesContent)
        etMinutesSummary = view.findViewById(R.id.etMinutesSummary)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnSave = view.findViewById(R.id.btnSave)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        progressBar = view.findViewById(R.id.progressBar)

        // Only organizers can edit
        btnEdit.visibility = if (isOrganizer) View.VISIBLE else View.GONE
        btnSave.visibility = View.GONE
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(MinutesApiService::class.java)
        val repository = MinutesRepository(apiService)
        val factory = MinutesViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MinutesViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.minutesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MinutesState.Loading -> {
                    showLoading(true)
                }
                is MinutesState.Success -> {
                    showLoading(false)
                    val minutes = state.data
                    minutesId = minutes.id
                    displayMinutes(minutes.summary, minutes.isFinal)
                }
                is MinutesState.Error -> {
                    showLoading(false)
                    // No minutes yet, show empty state
                    showEmptyState()
                }
                is MinutesState.Idle -> {
                    showLoading(false)
                }
            }
        }

        viewModel.isEditMode.observe(viewLifecycleOwner) { editMode ->
            updateEditMode(editMode)
        }
    }

    private fun loadMinutes() {
        viewModel.loadMinutesByAgenda(agendaId)
    }

    private fun displayMinutes(summary: String, isFinal: Boolean) {
        tvMinutesContent.text = summary
        tvMinutesContent.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        // Show edit button for organizers (no approval workflow)
        if (isOrganizer) {
            btnEdit.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        tvMinutesContent.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE

        if (isOrganizer) {
            tvEmptyState.text = "No minutes yet. Click Edit to add minutes."
            btnEdit.visibility = View.VISIBLE
        } else {
            tvEmptyState.text = "No minutes available yet."
        }
    }

    private fun enableEditMode(enabled: Boolean) {
        viewModel.setEditMode(enabled)

        if (enabled) {
            // Populate edit text with current content
            etMinutesSummary.setText(tvMinutesContent.text)
        }
    }

    private fun updateEditMode(editMode: Boolean) {
        if (editMode) {
            tvMinutesContent.visibility = View.GONE
            etMinutesSummary.visibility = View.VISIBLE
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
        } else {
            tvMinutesContent.visibility = View.VISIBLE
            etMinutesSummary.visibility = View.GONE
            // Only show edit button for organizers (ADMIN/SUPER_ADMIN)
            btnEdit.visibility = if (isOrganizer) View.VISIBLE else View.GONE
            btnSave.visibility = View.GONE
        }
    }

    private fun saveMinutes() {
        val summary = etMinutesSummary.text.toString().trim()

        if (summary.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter minutes summary", Toast.LENGTH_SHORT).show()
            return
        }

        if (minutesId == null) {
            // Create new minutes
            viewModel.saveMinutes(
                agendaId = agendaId,
                summary = summary,
                decisions = emptyList(),
                actionItems = emptyList(),
                isFinal = false
            ) { result ->
                handleSaveResult(result)
            }
        } else {
            // Update existing minutes
            viewModel.updateMinutes(
                id = minutesId!!,
                agendaId = agendaId,
                summary = summary,
                decisions = emptyList(),
                actionItems = emptyList(),
                isFinal = false
            ) { result ->
                handleSaveResult(result)
            }
        }
    }

    private fun handleSaveResult(result: Result<com.mgb.mrfcmanager.data.remote.dto.MeetingMinutesDto>) {
        when (result) {
            is Result.Success -> {
                Toast.makeText(requireContext(), "Minutes saved", Toast.LENGTH_SHORT).show()
                enableEditMode(false)
            }
            is Result.Error -> {
                showError("Failed to save: ${result.message}")
            }
            is Result.Loading -> {
                // Loading state
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
