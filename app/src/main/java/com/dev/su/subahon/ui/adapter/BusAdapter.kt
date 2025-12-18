package com.dev.su.subahon.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.BusModel

class BusAdapter(private val busList: List<BusModel>) :
    RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_expandable, parent, false)
        return BusViewHolder(view)
    }

    override fun getItemCount() = busList.size

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = busList[position]

        holder.tvBusName.text = "${bus.busNo} – ${bus.tripType}"
        holder.tvDriver.text = "Driver: ${bus.driverName} (${bus.driverPhone})"
        holder.tvRoute.text = bus.stops.joinToString(" → ")

        holder.layoutExpand.visibility = if (bus.isExpanded) View.VISIBLE else View.GONE
        holder.ivExpand.rotation = if (bus.isExpanded) 180f else 0f

        holder.ivExpand.setOnClickListener {
            bus.isExpanded = !bus.isExpanded
            notifyItemChanged(position)
        }

        // Schedule
        holder.layoutSchedule.removeAllViews()
        bus.schedule.forEach { (day, times) ->
            val tv = TextView(holder.itemView.context)
            tv.text = "$day : ${times.joinToString(", ")}"
            tv.textSize = 13f
            tv.setTextColor(Color.DKGRAY)
            holder.layoutSchedule.addView(tv)
        }
    }

    class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBusName: TextView = view.findViewById(R.id.tvBusName)
        val tvDriver: TextView = view.findViewById(R.id.tvDriver)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpand)
        val layoutExpand: LinearLayout = view.findViewById(R.id.layoutExpand)
        val tvRoute: TextView = view.findViewById(R.id.tvRoute)
        val layoutSchedule: LinearLayout = view.findViewById(R.id.layoutSchedule)
    }
}
