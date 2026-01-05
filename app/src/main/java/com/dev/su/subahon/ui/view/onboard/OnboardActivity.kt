package com.dev.su.subahon.ui.view.onboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.dev.su.subahon.R
import com.dev.su.subahon.databinding.ActivityOnboardBinding
import com.dev.su.subahon.ui.view.activity.AuthActivity
import com.dev.su.subahon.ui.view.activity.MainActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val btnNext = findViewById<Button>(R.id.btnNext)

        viewPager.adapter = OnboardAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        btnNext.setOnClickListener {
            if (viewPager.currentItem < 2) {
                viewPager.currentItem += 1
            } else {
                getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("first_run", false).apply()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }
    }
}
