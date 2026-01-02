package com.dev.su.subahon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.databinding.ItemUserAdminBinding

class AdminAdapter (
    private val users: List<User>
) : RecyclerView.Adapter<AdminAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserAdminBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val user = users[position]

        holder.binding.tvName.text = user.name
        holder.binding.tvEmail.text = user.email
        holder.binding.tvStudentId.text = "ID: ${user.studentId}"

        holder.binding.tvRole.text = user.role.uppercase()

        val color = when (user.role) {
            "student" -> "#4CAF50".toColorInt()
            "driver" -> "#2196F3".toColorInt()
            "admin" -> "#F44336".toColorInt()
            else -> "#FFC107".toColorInt()
        }

        holder.binding.tvRole.setBackgroundColor(color)
    }

    override fun getItemCount() = users.size
}