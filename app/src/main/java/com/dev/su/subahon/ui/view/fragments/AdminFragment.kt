package com.dev.su.subahon.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.databinding.FragmentAdminBinding
import com.dev.su.subahon.ui.adapter.AdminAdapter
import com.dev.su.subahon.utils.FirebaseUtil

class AdminFragment : Fragment() {

    private lateinit var binding: FragmentAdminBinding
    private val users = mutableListOf<User>()
    private lateinit var adapter: AdminAdapter

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

        FirebaseUtil.firestore.collection("users")
            .addSnapshotListener { snapshots, _ ->
                users.clear()
                snapshots?.forEach { doc ->
                    doc.toObject(User::class.java).let {
                        users.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }
}