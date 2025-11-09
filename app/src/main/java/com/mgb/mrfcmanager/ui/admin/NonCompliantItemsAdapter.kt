package com.mgb.mrfcmanager.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.NonCompliantItemDto

/**
 * Adapter for displaying non-compliant items
 */
class NonCompliantItemsAdapter : ListAdapter<NonCompliantItemDto, NonCompliantItemsAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_non_compliant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSectionName: TextView = itemView.findViewById(R.id.tvSectionName)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        private val tvPageNumber: TextView = itemView.findViewById(R.id.tvPageNumber)

        fun bind(item: NonCompliantItemDto) {
            tvSectionName.text = item.section
            tvItemDescription.text = item.itemDescription
            
            if (item.pageNumber != null) {
                tvPageNumber.text = "Page ${item.pageNumber}"
                tvPageNumber.visibility = View.VISIBLE
            } else {
                tvPageNumber.visibility = View.GONE
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NonCompliantItemDto>() {
        override fun areItemsTheSame(
            oldItem: NonCompliantItemDto,
            newItem: NonCompliantItemDto
        ): Boolean {
            return oldItem.section == newItem.section && 
                   oldItem.itemDescription == newItem.itemDescription
        }

        override fun areContentsTheSame(
            oldItem: NonCompliantItemDto,
            newItem: NonCompliantItemDto
        ): Boolean {
            return oldItem == newItem
        }
    }
}

