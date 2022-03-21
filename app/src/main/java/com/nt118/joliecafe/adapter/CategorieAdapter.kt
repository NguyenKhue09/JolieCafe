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
    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val tvCart: TextView = itemView.findViewById(R.id.tvCart)
        val imgCart : ImageView = itemView.findViewById(R.id.imgCart)
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
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_categories,parent,false),mlistener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgCart.setImageResource(item[position].image)
        holder.tvCart.text = item[position].title
    }

    override fun getItemCount(): Int {
        return item.size
    }
}