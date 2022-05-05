package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R

class CartAdapterDemo : RecyclerView.Adapter<CartAdapterDemo.ViewHolder>() {
    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return
    }

    override fun getItemCount() = 3

}