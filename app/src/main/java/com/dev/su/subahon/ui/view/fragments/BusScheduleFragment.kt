package com.dev.su.subahon.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.BusModel
import com.dev.su.subahon.ui.adapter.BusAdapter

class BusScheduleFragment : Fragment() {

    private lateinit var rvBusSchedule: RecyclerView
    private lateinit var adapter: BusAdapter
    private lateinit var busList: List<BusModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bus_schedule, container, false)
        rvBusSchedule = view.findViewById(R.id.rvBusSchedule)
        rvBusSchedule.layoutManager = LinearLayoutManager(requireContext())
        busList = getBusData()
        adapter = BusAdapter(busList)
        rvBusSchedule.adapter = adapter
        return view
    }

    private fun getBusData(): List<BusModel> {
        return listOf(
            BusModel(
                busNo = "Bus #01",
                tripType = "Afternoon Incoming",
                driverName = "Md. Rajan",
                driverPhone = "01935768889",
                stops = listOf("Technical", "Mirpur-1/2", "Mirpur-10", "Green Road Campus"),
                schedule = mapOf(
                    "Saturday" to listOf("10:30", "10:40", "10:50", "11:05", "11:30"),
                    "Sunday" to listOf("10:30", "10:40", "10:50", "11:05", "11:30")
                )
            ),
            BusModel(
                busNo = "Bus #02",
                tripType = "Afternoon Outgoing",
                driverName = "Md. Fahim",
                driverPhone = "01778246239",
                stops = listOf("Green Road Campus", "Malibagh Flyover", "Rajarbag Police Line", "Mugrapara"),
                schedule = mapOf(
                    "Saturday" to listOf("12:30", "12:35", "12:40", "12:45", "12:55"),
                    "Sunday" to listOf("12:30", "12:35", "12:40", "12:45", "12:55")
                )
            )
        )
    }
}