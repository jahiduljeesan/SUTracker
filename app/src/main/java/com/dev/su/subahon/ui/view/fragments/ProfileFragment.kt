package com.dev.su.subahon.ui.view.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dev.su.subahon.R
import com.dev.su.subahon.databinding.FragmentProfileBinding
import com.dev.su.subahon.ui.view.activity.AuthActivity
import com.dev.su.subahon.ui.viewmodel.ProfileViewModel
import com.dev.su.subahon.utils.FirebaseUtil

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startUserListener()
        viewModel.user.observe(viewLifecycleOwner) {user->
            binding.tvUserName.text = user?.name?.trim()
            binding.tvUserEmail.text = user?.email?.trim()
            Log.d("ProfileFragment", "User Admin Status: ${user?.admin}")
            if (user?.admin ?: false) {
                binding.optionAdmin.visibility = View.VISIBLE
            }else {
                binding.optionAdmin.visibility = View.GONE
            }
        }

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logging out..", Toast.LENGTH_SHORT).show()
            FirebaseUtil.auth.signOut()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            requireActivity().finish()
        }

        binding.optionAboutDev.setOnClickListener {
            findNavController().navigate(R.id.fragProfile_to_fragDevInfo)
        }

    }

}