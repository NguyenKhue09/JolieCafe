package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R
import com.nt118.joliecafe.models.CategorieModel

class CategorieAdapter(private val item : ArrayList<CategorieModel>) : RecyclerView.Adapter<CategorieAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvCart: TextView = itemView.findViewById(R.id.tvCart)
        val imgCart : ImageView = itemView.findViewById(R.id.imgCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_categories,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgCart.setImageResource(item[position].image)
        holder.tvCart.text = item[position].title
    }

    override fun getItemCount(): Int {
        return item.size
    }
}