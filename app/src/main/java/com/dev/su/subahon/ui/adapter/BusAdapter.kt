package com.dev.su.subahon.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.BusModel

class BusAdapter(
    private val list: List<BusModel>,
    private var selectedDay: String
) : RecyclerView.Adapter<BusAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val busNo: TextView = v.findViewById(R.id.tvBusNo)
        val trip: TextView = v.findViewById(R.id.tvTrip)
        val driver: TextView = v.findViewById(R.id.tvDriver)
        val stops: TextView = v.findViewById(R.id.tvStops)
        val schedule: TextView = v.findViewById(R.id.tvSchedule)
        val expand: LinearLayout = v.findViewById(R.id.layoutExpand)
        val arrow: ImageView = v.findViewById(R.id.imgExpand)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        return VH(LayoutInflater.from(p.context).inflate(R.layout.item_bus, p, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val bus = list[i]

        h.busNo.text = bus.busNo
        h.trip.text = bus.tripType
        h.driver.text = "Driver: ${bus.driver.name} | ${bus.driver.phone}"

        h.expand.visibility = if (bus.expanded) View.VISIBLE else View.GONE
        h.arrow.rotation = if (bus.expanded) 180f else 0f

        // Stops (wrapped text)
        h.stops.text = "Stops:\n• " + bus.stops.joinToString(" → ")

        // Schedule (day-based)
        val times = bus.schedule[selectedDay] ?: emptyList()
        h.schedule.text =
            if (times.isEmpty()) "No service on $selectedDay"
            else "Schedule ($selectedDay):\n" + times.joinToString("   ")

        h.itemView.setOnClickListener {
            h.arrow.setImageResource(
                if (bus.expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )
            bus.expanded = !bus.expanded
            notifyItemChanged(i)
        }
    }

    fun updateDay(day: String) {
        selectedDay = day
        notifyDataSetChanged()
    }
}
