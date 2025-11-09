package com.mgb.mrfcmanager.ui.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.ComplianceSectionDto
import java.util.Locale

/**
 * Adapter for displaying compliance sections with progress
 */
class ComplianceSectionsAdapter : ListAdapter<ComplianceSectionDto, ComplianceSectionsAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compliance_section, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSectionName: TextView = itemView.findViewById(R.id.tvSectionName)
        private val tvSectionPercentage: TextView = itemView.findViewById(R.id.tvSectionPercentage)
        private val tvSectionDetails: TextView = itemView.findViewById(R.id.tvSectionDetails)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(section: ComplianceSectionDto) {
            tvSectionName.text = section.sectionName
            tvSectionPercentage.text = String.format(Locale.US, "%.0f%%", section.percentage)
            
            val applicableItems = section.total - section.na
            tvSectionDetails.text = "${section.compliant} of $applicableItems items compliant"

            // Set progress bar
            progressBar.progress = section.percentage.toInt()

            // Set color based on percentage
            val color = when {
                section.percentage >= 90.0 -> Color.parseColor("#4CAF50") // Green
                section.percentage >= 70.0 -> Color.parseColor("#FF9800") // Orange
                else -> Color.parseColor("#F44336") // Red
            }
            tvSectionPercentage.setTextColor(color)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ComplianceSectionDto>() {
        override fun areItemsTheSame(
            oldItem: ComplianceSectionDto,
            newItem: ComplianceSectionDto
        ): Boolean {
            return oldItem.sectionName == newItem.sectionName
        }

        override fun areContentsTheSame(
            oldItem: ComplianceSectionDto,
            newItem: ComplianceSectionDto
        ): Boolean {
            return oldItem == newItem
        }
    }
}

