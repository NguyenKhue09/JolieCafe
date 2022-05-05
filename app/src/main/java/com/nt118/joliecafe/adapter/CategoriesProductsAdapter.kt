package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemRvCategoriesProductsBinding
import com.nt118.joliecafe.models.CategorieModel

class CategoriesProductsAdapter(private val item : ArrayList<CategorieModel>) : RecyclerView.Adapter<CategoriesProductsAdapter.ViewHolder>() {

    class ViewHolder(var binding: ItemRvCategoriesProductsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvCategoriesProductsBinding.inflate(layoutInflater)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
         return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.imgCategoriesProducts.setImageResource(item[position].image)
        holder.binding.tvCategoriesProducts.text = item[position].title
    }

    override fun getItemCount(): Int {
        return item.size
    }
}