package com.dev.su.subahon.ui.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dev.su.subahon.R
import com.dev.su.subahon.databinding.FragmentLoginBinding
import com.dev.su.subahon.ui.view.activity.MainActivity
import com.dev.su.subahon.utils.LoginSignupAnimationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }


        binding.btnLogin.setOnClickListener {
            setLogin()
        }

        binding.btnForgotPass.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success...")
                        .setMessage("Please check your email for reset link")
                        .setPositiveButton("Okay") {
                            dialog,_ ->
                            dialog.dismiss()
                        }
                        .create().show()
                }else {
                    Toast.makeText(requireContext(), "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

    }
    fun setLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPass.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        LoginSignupAnimationHelper.showAnimation(binding.lottieAnimation)

        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    var role = ""
                   firestore.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                 role = documentSnapshot.getString("role") ?: ""
                            }
                        }

                    LoginSignupAnimationHelper.showSuccessAnimation(binding.lottieAnimation) {
                        if(role == "student" || role == "driver" ) {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        } else {
                            AlertDialog.Builder(requireContext()).setTitle("Request Pending....")
                                .setMessage("Authority will approve your joining request")
                                .setPositiveButton("Okay") {dialog,_ ->
                                    requireActivity().finish()
                                }.create().show()
                        }
                    }

                }else {
                    Toast.makeText(requireContext(), "Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    LoginSignupAnimationHelper.hideAnimation(binding.lottieAnimation)
                }
            }
    }


}