package com.dev.su.subahon.ui.view.onboard

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.su.subahon.R

class OnboardAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val pages = listOf(
        OnboardFragment(
            R.drawable.bus_tracking,
            "Track University Bus",
            "See real-time bus location for your university"
        ),
        OnboardFragment(
            R.drawable.live_map,
            "Live Map View",
            "Track buses live on Google Map"
        ),
        OnboardFragment(
            R.drawable.eta,
            "Accurate ETA",
            "Know arrival time before reaching stop"
        )
    )

    override fun getItemCount() = pages.size
    override fun createFragment(position: Int) = pages[position]
}
