package com.dev.su.subahon.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.databinding.AddDriverAllertStyleBinding
import com.dev.su.subahon.databinding.FragmentAdminBinding
import com.dev.su.subahon.ui.adapter.AdminAdapter
import com.dev.su.subahon.ui.viewmodel.ProfileViewModel
import com.dev.su.subahon.utils.FirebaseUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AdminFragment : Fragment() {

    private lateinit var binding: FragmentAdminBinding
    private lateinit var adapter: AdminAdapter
    private val users = mutableListOf<User>()
    private val profileVM: ProfileViewModel  by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = AdminAdapter(users) {user->
            if (user.admin) return@AdminAdapter

            when(user.role) {
                "student" -> studentDialog(user)
                "driver" -> driverDialog(user)
                "none" -> noneDialog(user)
                "blocked" -> blockedDialog(user)
            }

        }
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        observeUsers()
        setupSearch()
        setupFilter()
    }

    private fun blockedDialog(user: User) {
        dialogHelper(
            "Blocked User",
            "What to you want to do",
            "Unblock",
            positiveAction = {
                profileVM.setUser(user.email) {
                    it.reference.update("role", "student")
                }
            },
            "Cancel",
            negativeAction = {

            },
        )
    }

    private fun noneDialog(user: User) {
        dialogHelper(
            "User pending",
            "What to you want to do",
            "Approve",
            positiveAction = {
                profileVM.setUser(user.email) {
                    it.reference.update("role", "student")
                }
            },
            "Cancel",
            negativeAction = {

            },
        )
    }

    private fun driverDialog(user: User) {
        dialogHelper(
            "Driver",
            "What to you want to do",
            "Make Student",
            positiveAction = {
                profileVM.setUser(user.email) {
                    it.reference.update("role", "student")
                }
            },
            "Cancel",
            negativeAction = {


            },
            "Block",
            neutralAction = {
                profileVM.setUser(user.email) {
                    it.reference.update("role", "blocked")
                }
            }
        )
    }

    private fun studentDialog(user: User) {
        dialogHelper(
            "Student",
            "What to you want to do",
            "Block",
            positiveAction = {
                profileVM.setUser(user.email) {
                    it.reference.update("role", "blocked")
                }
            },
            "Cancel",
            negativeAction = {},
            "Make Driver",
            neutralAction = {
                val binding = AddDriverAllertStyleBinding.inflate(LayoutInflater.from(requireContext()))

                val routes = listOf("bus1", "bus2", "bus3", "bus4")
                binding.spRoute.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    routes
                )

                val alertDialog = AlertDialog.Builder(requireContext())
                    .setView(binding.root)
                    .setCancelable(false)
                    .create()

                binding.btnCancel.setOnClickListener {
                    alertDialog.dismiss()
                }

                binding.btnAdd.setOnClickListener {
                    val routeId = binding.spRoute.selectedItem?.toString()?.trim().orEmpty()
                    val phone = binding.etPhone.text?.toString()?.trim().orEmpty()

                    if (phone.isEmpty()) {
                        binding.etPhone.error = "Phone number required"
                        return@setOnClickListener
                    }

                    if (phone.length < 10) {
                        binding.etPhone.error = "Enter a valid phone number"
                        return@setOnClickListener
                    }

                    profileVM.setUser(user.email) {
                        it.reference.update(
                            mapOf(
                                "role" to "driver",
                                "routeId" to routeId,
                                "phone" to phone
                            )
                        )
                    }

                    alertDialog.dismiss()
                }

                alertDialog.show()
            }

        )
    }

    private fun dialogHelper(
        title: String,
        message: String,
        positiveText: String,
        positiveAction: () -> Unit,
        negativeText: String,
        negativeAction: () -> Unit,
        neutralText: String = "",
        neutralAction: () -> Unit = {},
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                positiveAction()
                dialog.cancel()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                negativeAction()
                dialog.cancel()
            }
            .setNeutralButton(neutralText) { dialog, _ ->
                neutralAction()
                dialog.cancel()
            }
            .show()
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
        val roles = arrayOf("All", "Student", "Driver","None","Blocked")

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Role")
            .setItems(roles) { _, which ->
                val role = when (which) {
                    1 -> "student"
                    2 -> "driver"
                    3 -> "none"
                    4 -> "blocked"
                    else -> "all"
                }
                adapter.filterByRole(role)
            }
            .show()
    }
}
