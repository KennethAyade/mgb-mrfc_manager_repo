package com.mgb.mrfcmanager.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto

/**
 * Adapter for displaying MRFCs with checkboxes for access selection
 */
class MrfcCheckboxAdapter : RecyclerView.Adapter<MrfcCheckboxAdapter.MrfcCheckboxViewHolder>() {

    private val mrfcs = mutableListOf<MrfcDto>()
    private val selectedMrfcIds = mutableSetOf<Long>()

    fun submitList(newMrfcs: List<MrfcDto>) {
        mrfcs.clear()
        mrfcs.addAll(newMrfcs)
        notifyDataSetChanged()
    }

    fun setSelectedMrfcs(mrfcIds: List<Long>) {
        selectedMrfcIds.clear()
        selectedMrfcIds.addAll(mrfcIds)
        notifyDataSetChanged()
    }

    fun getSelectedMrfcIds(): List<Long> {
        return selectedMrfcIds.toList()
    }

    fun selectAll() {
        selectedMrfcIds.clear()
        mrfcs.forEach { selectedMrfcIds.add(it.id) }
        notifyDataSetChanged()
    }

    fun clearAll() {
        selectedMrfcIds.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MrfcCheckboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mrfc_checkbox, parent, false)
        return MrfcCheckboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: MrfcCheckboxViewHolder, position: Int) {
        val mrfc = mrfcs[position]
        holder.bind(mrfc, selectedMrfcIds.contains(mrfc.id))
    }

    override fun getItemCount(): Int = mrfcs.size

    inner class MrfcCheckboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkboxMrfc)
        private val tvMrfcName: TextView = itemView.findViewById(R.id.tvMrfcName)
        private val tvMrfcLocation: TextView = itemView.findViewById(R.id.tvMrfcLocation)

        fun bind(mrfc: MrfcDto, isSelected: Boolean) {
            tvMrfcName.text = mrfc.name
            tvMrfcLocation.text = "${mrfc.municipality}, ${mrfc.province}"
            checkBox.isChecked = isSelected

            // Handle checkbox state change
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMrfcIds.add(mrfc.id)
                } else {
                    selectedMrfcIds.remove(mrfc.id)
                }
            }

            // Handle item click (toggle checkbox)
            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }
}

