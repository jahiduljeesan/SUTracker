package com.dev.su.subahon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.data.model.BusDisplay
import com.dev.su.subahon.databinding.RouteListStyleBinding

class BusListAdapter(
    private var buses: List<BusDisplay>,
    private var onCallClick:(phone: String) -> Unit
):
    RecyclerView.Adapter<BusListAdapter.BusVH>() {

    inner class BusVH(val binding: RouteListStyleBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusVH {
        val binding = RouteListStyleBinding.
        inflate(LayoutInflater.from(parent.context),parent,false)
        return BusVH(binding)
    }

    override fun onBindViewHolder(
        holder: BusVH,
        position: Int
    ) {
        val bus = buses[position]
        with(holder.binding) {
            tvBusName.text = bus.busName
            tvRouteName.text = "Route: ${bus.routeName}"
            tvHeading.text = "Heading: ${bus.heading}"

            btnCall.setOnClickListener {
                onCallClick(bus.phone)
            }
        }
    }


    fun updateList(newList: List<BusDisplay>) {
        buses = newList
        notifyDataSetChanged()
    }




    override fun getItemCount(): Int = buses.size


}