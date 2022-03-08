package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R


class BestSallerAdapter(private val item : ArrayList<String>) : RecyclerView.Adapter<BestSallerAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvCart: TextView = itemView.findViewById(R.id.item_bestsaller)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_bestsaller,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCart.text = item[position]
    }

    override fun getItemCount(): Int {
        return item.size
    }
}