package com.dev.su.subahon.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.databinding.ItemUserAdminBinding
import androidx.core.graphics.toColorInt
import com.dev.su.subahon.R

class AdminAdapter(
    private val fullList: MutableList<User>
) : RecyclerView.Adapter<AdminAdapter.UserVH>() {

    private val displayList = mutableListOf<User>()

    init {
        displayList.addAll(fullList)
    }

    inner class UserVH(val binding: ItemUserAdminBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val binding = ItemUserAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserVH(binding)
    }

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val user = displayList[position]

        holder.binding.tvName.text = user.name
        holder.binding.tvEmail.text = user.email
        holder.binding.tvStudentId.text = "ID: ${user.studentId}"

        holder.binding.tvRole.text = if (user.admin)"ADMIN" else user.role.uppercase()

        var bg = when (user.role) {
            "student" -> R.drawable.bg_role_green
            "driver" -> R.drawable.bg_role_driver
            else -> R.drawable.bg_role_gray
        }

        if (user.admin) bg =  R.drawable.bg_role_admin
        holder.binding.tvRoleLayout.setBackgroundResource(bg)
    }

    override fun getItemCount() = displayList.size

    fun filterByQuery(query: String) {
        displayList.clear()

        if (query.isBlank()) {
            displayList.addAll(fullList)
        } else {
            val q = query.lowercase()
            displayList.addAll(
                fullList.filter {
                    it.name.lowercase().contains(q) ||
                            it.email.lowercase().contains(q) ||
                            it.studentId.lowercase().contains(q)
                }
            )
        }
        notifyDataSetChanged()
    }

    fun filterByRole(role: String) {
        displayList.clear()

        if (role == "all") {
            displayList.addAll(fullList)
        } else {
            displayList.addAll(fullList.filter { it.role == role })
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<User>) {
        fullList.clear()
        fullList.addAll(newList)
        displayList.clear()
        displayList.addAll(newList)
        notifyDataSetChanged()
    }
}
