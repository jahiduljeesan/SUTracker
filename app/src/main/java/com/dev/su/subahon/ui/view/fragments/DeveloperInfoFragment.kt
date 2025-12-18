package com.dev.su.subahon.ui.view.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.Developer
import com.dev.su.subahon.databinding.FragmentDeveloperInfoBinding
import com.dev.su.subahon.ui.adapter.DevAdapter


class DeveloperInfoFragment : Fragment() {
    private lateinit var binding: FragmentDeveloperInfoBinding
    private val developerList = mutableListOf<Developer>()
    private lateinit var adapter: DevAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeveloperInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //adding developers list
        addDeveloperList()

        binding.rvDevList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = DevAdapter(
            onMailClick = { mailId ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822" // only email apps will respond
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(mailId))
                    putExtra(Intent.EXTRA_SUBJECT, "SU-Bahon Query")
                }

                try {
                    startActivity(Intent.createChooser(intent, "Send Email"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                }
            }

            ,

            onFacebookClick = { facebookId ->
                val facebookUrl = "https://www.facebook.com/$facebookId"
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("fb://facewebmodal/f?href=$facebookUrl")
                    startActivity(intent)
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)))
                }
            },

            onWhatsappClick = { whatsappNumber ->
                val url = "https://wa.me/$whatsappNumber"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            },

            onInstagramClick = { instagramId ->
                val uri = Uri.parse("http://instagram.com/_u/$instagramId")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.instagram.android")

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // fallback to browser
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/$instagramId")))
                }
            }
        )

        binding.rvDevList.adapter = adapter

        adapter.submitList(developerList)
    }

    private fun addDeveloperList() {
        //Zobayer
        developerList.add(Developer(
            R.drawable.zubayer_image,
            "Zubayer Hossain",
            "CSE2201025075",
            "CSE",
            "25M1",
            "hmzobayer2002@gmail.com",
            "profile.php?id=61578218090198",
            "8801312470264",
            "zubayer_hossain_1"
        ))

        //Mustafiz
        developerList.add(Developer(
            R.drawable.mustafiz_image,
            "Mustafizur Rahman",
            "CSE2201025070",
            "CSE",
            "25M1",
            "mrcse2201025070@gmail.com",
            "share/1LUnWe8kca/",
            "8801518670079",
            "me.mustafiz"
        ))

        //Zannatul
        developerList.add(Developer(
            R.drawable.jannatul_image,
            "Zannatul Ferdous",
            "CSE2201025007",
            "CSE",
            "25M1",
            "zannatulf83@gmail.com",
            "shopner.ranihimu",
            "001738351353",
            "zannatulf83"
        ))

        //Nahid
        developerList.add(Developer(
            R.drawable.nahid_image,
            "Md.Nahid Hasan",
            "CSE2201025036",
            "CSE",
            "25M1",
            "21nahidhassan@gmail.com",
            "share/19BZUz3sbP/",
            "8801790797261",
            "https://www.instagram.com/invites/contact/?igsh=1tlx4lshd7qyq&utm_content=6m9537d"
        ))
    }
}