package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R

class ProductAdapter(private val item : ArrayList<String>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val tvCart: TextView = itemView.findViewById(R.id.tv_categories_product)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition)
            }
        }
    }


    private lateinit var mlistener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
        mlistener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_product,parent,false),mlistener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCart.text = item[position]
    }

    override fun getItemCount(): Int {
        return item.size
    }
}