package com.dev.su.subahon.ui.view.fragments

import android.app.DownloadManager.Query
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.databinding.FragmentAdminBinding
import com.dev.su.subahon.ui.adapter.AdminAdapter
import com.dev.su.subahon.utils.FirebaseUtil

class AdminFragment : Fragment() {

    private lateinit var binding: FragmentAdminBinding
    private lateinit var adapter: AdminAdapter
    private val users = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = AdminAdapter(users)
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        observeUsers()
        setupSearch()
        setupFilter()
    }

    private fun observeUsers() {
        FirebaseUtil.firestore.collection("users")
            .addSnapshotListener { snapshots, _ ->
                val list = snapshots?.documents
                    ?.mapNotNull { it.toObject(User::class.java) }
                    ?.sortedByDescending { it.admin }
                    ?: emptyList()

                adapter.updateData(list)
            }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filterByQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupFilter() {
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val roles = arrayOf("All", "Student", "Driver","Pending")

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Role")
            .setItems(roles) { _, which ->
                val role = when (which) {
                    1 -> "student"
                    2 -> "driver"
                    4 -> "none"
                    else -> "all"
                }
                adapter.filterByRole(role)
            }
            .show()
    }
}
