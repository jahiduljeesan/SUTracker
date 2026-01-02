package com.dev.su.subahon.ui.view.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dev.su.subahon.ui.view.activity.MainActivity
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.databinding.FragmentSignupBinding
import com.dev.su.subahon.utils.LoginSignupAnimationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_signup_to_login)
        }
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signup_to_login)
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnSignup.setOnClickListener {
            setData()
        }
    }
    fun setData(){
        val name = binding.etName.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPass.text.toString().trim()
        val confirmPassword = binding.etConfirmPass.text.toString().trim()


        // Validate fields
        if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()) {

            if (password.length < 6){
                binding.etPassLayout.error = "Password must be minimum 6 character"
            }

            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            binding.etPassLayout.error = "Passwords do not match"
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        LoginSignupAnimationHelper.showAnimation(binding.lottieAnimation)
        //register user
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val user = User(
                        name = name,
                        email = email,
                        phone = "",
                        role = "none",
                        routeId = "",
                        studentId = studentId,
                        heading = "",
                        status = "",
                        isAdmin = false,
                    )

                    firestore.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            LoginSignupAnimationHelper.showSuccessAnimation(binding.lottieAnimation) {
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                requireActivity().finish()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Firestore Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            LoginSignupAnimationHelper.hideAnimation(binding.lottieAnimation)
                        }
                } else {
                    Toast.makeText(requireContext(), "Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    LoginSignupAnimationHelper.hideAnimation(binding.lottieAnimation)
                }
            }
    }
}