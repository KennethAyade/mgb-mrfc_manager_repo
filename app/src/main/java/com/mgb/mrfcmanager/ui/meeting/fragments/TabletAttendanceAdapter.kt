package com.mgb.mrfcmanager.ui.meeting.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto

/**
 * Tablet-based attendance adapter
 * Shows each attendee as a tablet card icon
 * Each tablet represents one user who logged attendance
 */
class TabletAttendanceAdapter(
    private val attendanceList: List<AttendanceDto>,
    private val onTabletClick: (AttendanceDto, Int) -> Unit
) : RecyclerView.Adapter<TabletAttendanceAdapter.TabletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tablet_card, parent, false)
        return TabletViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabletViewHolder, position: Int) {
        holder.bind(attendanceList[position], position + 1, onTabletClick)
    }

    override fun getItemCount() = attendanceList.size

    class TabletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivTabletIcon: ImageView = itemView.findViewById(R.id.ivTabletIcon)
        private val tvTabletNumber: TextView = itemView.findViewById(R.id.tvTabletNumber)
        private val tvAttendeeName: TextView = itemView.findViewById(R.id.tvAttendeeName)

        fun bind(
            attendance: AttendanceDto,
            tabletNumber: Int,
            onClick: (AttendanceDto, Int) -> Unit
        ) {
            tvTabletNumber.text = "Tablet $tabletNumber"
            tvAttendeeName.text = attendance.attendeeName ?: "Attendee $tabletNumber"

            // Set tablet icon color based on presence status
            val tintColor = if (attendance.isPresent) {
                itemView.context.getColor(R.color.status_success)
            } else {
                itemView.context.getColor(R.color.status_pending)
            }
            ivTabletIcon.setColorFilter(tintColor)

            // Set click listener
            itemView.setOnClickListener {
                onClick(attendance, tabletNumber)
            }

            // Add ripple effect feedback
            itemView.isClickable = true
            itemView.isFocusable = true
        }
    }
}
