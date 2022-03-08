package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemRowLayoutBinding

class FavoriteItemAdapter() : RecyclerView.Adapter<FavoriteItemAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: ItemRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {

        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRowLayoutBinding.inflate(layoutInflater)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    fun setData() {
//        val recipesDiffUtil = RecipesDiffUtil(recipes, newData.results)
//        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)
//        recipes = newData.results
//        diffUtilResult.dispatchUpdatesTo(this)
    }


}