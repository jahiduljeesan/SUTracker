package com.dev.su.subahon.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.BusModel
import com.dev.su.subahon.ui.adapter.BusAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusScheduleFragment : Fragment(R.layout.fragment_bus_schedule) {

    private lateinit var adapter: BusAdapter
    private val busList = mutableListOf<BusModel>()
    private var selectedDay = getToday()

    override fun onViewCreated(view: View, saved: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvBus)
        val btn = view.findViewById<MaterialButton>(R.id.btnDayFilter)

        adapter = BusAdapter(busList, selectedDay)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btn.text = selectedDay
        btn.setOnClickListener { showDayPicker(btn) }

        loadFromFirestore()
    }

    private fun loadFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("bus_service")
            .get()
            .addOnSuccessListener {
                busList.clear()
                for (doc in it) {
                    busList.add(doc.toObject(BusModel::class.java))
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showDayPicker(btn: MaterialButton) {
        val days = arrayOf(
            "Saturday","Sunday","Monday",
            "Tuesday","Wednesday","Thursday","Friday"
        )

        AlertDialog.Builder(requireContext())
            .setItems(days) { _, i ->
                selectedDay = days[i]
                btn.text = selectedDay
                adapter.updateDay(selectedDay)
            }.show()
    }

    private fun getToday(): String =
        SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
}
