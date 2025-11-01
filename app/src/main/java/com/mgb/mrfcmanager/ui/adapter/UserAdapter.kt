package com.mgb.mrfcmanager.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.dto.UserDto

class UserAdapter(
    private val onUserClick: (UserDto) -> Unit,
    private val onEditClick: (UserDto) -> Unit,
    private val onDeleteClick: (UserDto) -> Unit,
    private val onToggleStatusClick: (UserDto) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = listOf<UserDto>()
    private var currentUserRole: String = "USER"

    fun submitList(newUsers: List<UserDto>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun setCurrentUserRole(role: String) {
        currentUserRole = role
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
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        private val btnToggleStatus: MaterialButton = itemView.findViewById(R.id.btnToggleStatus)

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

            // Hide action buttons for SUPER_ADMIN users (cannot edit/delete SUPER_ADMIN)
            val isSuperAdmin = user.role == "SUPER_ADMIN"
            btnEdit.visibility = if (isSuperAdmin) View.GONE else View.VISIBLE
            btnDelete.visibility = if (isSuperAdmin) View.GONE else View.VISIBLE
            btnToggleStatus.visibility = if (isSuperAdmin) View.GONE else View.VISIBLE

            card.setOnClickListener {
                onUserClick(user)
            }

            btnEdit.setOnClickListener {
                onEditClick(user)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(user)
            }

            btnToggleStatus.setOnClickListener {
                onToggleStatusClick(user)
            }
        }
    }
}
