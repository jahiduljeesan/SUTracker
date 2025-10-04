package com.dev.su.subahon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.su.subahon.data.model.Developer
import com.dev.su.subahon.databinding.DeveloperInfoStyleBinding

class DevAdapter(
    private val onMailClick: (mail: String) -> Unit,
    private val onFacebookClick: (facebook:String) -> Unit,
    private val onWhatsappClick: (whatsapp:String) -> Unit,
    private val onInstagramClick: (instagram: String) -> Unit,
) : ListAdapter<Developer, DevAdapter.DevVH>(DiffCallBack()) {

    inner class DevVH(val binding: DeveloperInfoStyleBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevAdapter.DevVH {
        val binding = DeveloperInfoStyleBinding
            .inflate(LayoutInflater.from(parent.context),parent,false)
        return DevVH(binding)
    }

    override fun onBindViewHolder(holder: DevAdapter.DevVH, position: Int) {
        val dev = getItem(position)

        //we can use with block if it is not working
        holder.binding.apply {
            devImage.setImageResource(dev.devImage)
            devName.text = dev.devName
            devId.text = "ID: ${dev.devID}"
            devDept.text = "Department: ${dev.devDept}"
            devBatch.text = "Batch: ${dev.devBatch}"

            btnMail.setOnClickListener {
                onMailClick(dev.devMail)
            }
            btnFacebook.setOnClickListener {
                onFacebookClick(dev.devFacebook)
            }
            btnWhatsapp.setOnClickListener {
                onWhatsappClick(dev.devWhatsapp)
            }
            btnInstagram.setOnClickListener {
                onInstagramClick((dev.devInstagram))
            }
        }
    }

    class DiffCallBack: DiffUtil.ItemCallback<Developer>() {
        override fun areItemsTheSame(
            oldItem: Developer, newItem: Developer
        ) = oldItem.devImage == newItem.devImage

        override fun areContentsTheSame(
            oldItem: Developer, newItem: Developer
        ) = oldItem == newItem

    }
}