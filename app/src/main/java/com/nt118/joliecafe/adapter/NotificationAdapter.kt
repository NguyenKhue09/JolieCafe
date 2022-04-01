package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemRowLayoutBinding
import com.nt118.joliecafe.databinding.ItemRvNotificationBinding

class NotificationAdapter(private val item : ArrayList<String>) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: ItemRvNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvNotificationBinding.inflate(layoutInflater)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvIdBill.text = item[position]
    }

    override fun getItemCount(): Int {
        return  item.size
    }
}