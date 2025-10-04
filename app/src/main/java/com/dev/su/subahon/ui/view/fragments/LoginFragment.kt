package com.dev.su.subahon.ui.view.fragments

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
import com.dev.su.subahon.utils.FirebaseUtil
import com.dev.su.subahon.utils.LoginSignupAnimationHelper

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
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

    }
    fun setLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPass.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        LoginSignupAnimationHelper.showAnimation(binding.lottieAnimation)

        FirebaseUtil.auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    LoginSignupAnimationHelper.showSuccessAnimation(binding.lottieAnimation) {
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                } else {
                    LoginSignupAnimationHelper.hideAnimation(binding.lottieAnimation)
                    Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

//    private fun showAnimation() {
//        binding.lottieAnimation.apply {
//            visibility = View.VISIBLE
//            setAnimation("loading.json")
//            repeatCount = LottieDrawable.INFINITE
//            playAnimation()
//        }
//    }
//
//    private fun hideAnimation(){
//        binding.lottieAnimation.apply {
//            cancelAnimation()
//            visibility = View.GONE
//        }
//    }
//
//    private fun showSuccessAnimation(onComplete: () -> Unit){
//        binding.lottieAnimation.apply {
//            cancelAnimation()
//            setAnimation("loading_success.json")
//            repeatCount = 0
//            playAnimation()
//
//            addAnimatorListener(object : AnimatorListenerAdapter(){
//                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
//                    super.onAnimationEnd(animation, isReverse)
//                    visibility = View.GONE
//                    removeAnimatorListener(this)
//                    onComplete()
//                }
//            })
//        }
//    }

}