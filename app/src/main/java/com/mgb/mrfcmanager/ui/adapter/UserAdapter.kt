package com.mgb.mrfcmanager.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.UserDto

class UserAdapter(
    private val onUserClick: (UserDto) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = listOf<UserDto>()

    fun submitList(newUsers: List<UserDto>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardUser)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val chipRole: Chip = itemView.findViewById(R.id.chipRole)
        private val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)

        fun bind(user: UserDto) {
            tvUsername.text = "@${user.username}"
            tvFullName.text = user.fullName
            tvEmail.text = user.email

            chipRole.text = when (user.role) {
                "SUPER_ADMIN" -> "Super Admin"
                "ADMIN" -> "Admin"
                else -> "User"
            }

            chipStatus.text = if (user.isActive) "Active" else "Inactive"

            card.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}
