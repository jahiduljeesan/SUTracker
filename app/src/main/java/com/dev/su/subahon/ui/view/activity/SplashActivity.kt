package com.dev.su.subahon.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev.su.subahon.R
import com.dev.su.subahon.databinding.ActivitySplashBinding
import com.dev.su.subahon.utils.FirebaseUtil
import androidx.core.content.edit
import com.dev.su.subahon.ui.view.onboard.OnboardActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val fadeAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.text_fade_in)
        binding.tvAppName.startAnimation(fadeAnimation)
        val slideAnimation: Animation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.image_splash_anim)
        binding.splashImage.startAnimation(slideAnimation)

        slideAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                if (isLoggedIn()) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }else{
                    val first_run = getSharedPreferences("settings",MODE_PRIVATE).getBoolean("first_run",true)

                    startActivity(Intent(this@SplashActivity,
                        if (!first_run) AuthActivity::class.java else AuthActivity::class.java))
                }
            }
        })
    }

    private fun isLoggedIn(): Boolean {
        return FirebaseUtil.auth.currentUser != null
    }
}