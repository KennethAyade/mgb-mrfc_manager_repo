package com.mgb.mrfcmanager.ui.meeting.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.card.MaterialCardView
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
        val attendance = attendanceList[position]
        // Use tablet number from backend if available, otherwise fallback to position
        val tabletNum = attendance.tabletNumber ?: (position + 1)
        holder.bind(attendance, tabletNum, onTabletClick)
    }

    override fun getItemCount() = attendanceList.size

    class TabletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardPhoto: MaterialCardView = itemView.findViewById(R.id.cardPhoto)
        private val ivAttendeePhoto: ImageView = itemView.findViewById(R.id.ivAttendeePhoto)
        private val tvTabletNumber: TextView = itemView.findViewById(R.id.tvTabletNumber)
        private val tvAttendeeName: TextView = itemView.findViewById(R.id.tvAttendeeName)

        fun bind(
            attendance: AttendanceDto,
            tabletNumber: Int,
            onClick: (AttendanceDto, Int) -> Unit
        ) {
            tvTabletNumber.text = "Tablet $tabletNumber"
            tvAttendeeName.text = attendance.attendeeName ?: "Attendee $tabletNumber"

            // Load attendee photo if available
            if (!attendance.photoUrl.isNullOrEmpty()) {
                ivAttendeePhoto.imageTintList = null // Remove tint for actual photos
                ivAttendeePhoto.load(attendance.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                }
            } else {
                // Show default person icon with tint
                ivAttendeePhoto.setImageResource(R.drawable.ic_person)
                ivAttendeePhoto.imageTintList = android.content.res.ColorStateList.valueOf(
                    itemView.context.getColor(R.color.text_hint)
                )
            }

            // Set card border color based on presence status
            val strokeColor = if (attendance.isPresent) {
                itemView.context.getColor(R.color.status_success)
            } else {
                itemView.context.getColor(R.color.status_pending)
            }
            cardPhoto.strokeColor = strokeColor

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
