package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemCartSuggestionBinding


class MoreProductsAdapter(private val item : ArrayList<String>) : RecyclerView.Adapter<MoreProductsAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: ItemCartSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCartSuggestionBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvPriceProduct.text = item[position]
    }

    override fun getItemCount(): Int {
        return  item.size
    }
}