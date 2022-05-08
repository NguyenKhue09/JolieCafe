package com.nt118.joliecafe.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvCategoriesProductsBinding
import com.nt118.joliecafe.models.CategorieModel

class CategoriesProductsAdapter(private val item : ArrayList<CategorieModel>,private val currentPosition:Int, val clicked:(Int)->Unit) : RecyclerView.Adapter<CategoriesProductsAdapter.ViewHolder>() {

    private var positionOld = currentPosition
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(positionOld  == item.size){
            positionOld = 0
        }
        holder.binding.imgCategoriesProducts.setImageResource(item[position].image)
        holder.binding.tvCategoriesProducts.text = item[position].title
        holder.binding.btnCategoriesProducts.setOnClickListener {
            positionOld = position
            clicked.invoke(position)
            notifyDataSetChanged()
        }
        if(position == positionOld){
            holder.binding.imgCategoriesProducts.setBackgroundResource(R.drawable.circle_shape_choose)
            holder.binding.tvCategoriesProducts.setTextColor(Color.parseColor("#BC834B"))
        } else {
            holder.binding.imgCategoriesProducts.setBackgroundResource(R.drawable.circle_shape_opacity60)
            holder.binding.tvCategoriesProducts.setTextColor(Color.parseColor("#E4E4E4"))
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }
}