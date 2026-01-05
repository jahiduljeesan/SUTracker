package com.dev.su.subahon.ui.view.onboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dev.su.subahon.R

class OnboardFragment(
    private val image: Int,
    private val title: String,
    private val desc: String
) : Fragment(R.layout.fragment_onboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.imgIllustration).setImageResource(image)
        view.findViewById<TextView>(R.id.tvTitle).text = title
        view.findViewById<TextView>(R.id.tvDescription).text = desc
    }
}
