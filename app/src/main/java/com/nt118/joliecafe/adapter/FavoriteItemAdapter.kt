package com.nt118.joliecafe.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRowLayoutBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.detail.DetailActivity

class FavoriteItemAdapter(
    private val context: Context,
    diffUtil: DiffUtil.ItemCallback<Product>
) : PagingDataAdapter<Product, FavoriteItemAdapter.MyViewHolder>(diffCallback = diffUtil) {

    class MyViewHolder(var binding: ItemRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
        val favoriteProduct = getItem(position)

        holder.binding.itemCard.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            context.startActivity(intent)
        }

        favoriteProduct?.let {
            holder.binding.itemImg.load(favoriteProduct.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            holder.binding.itemName.text = favoriteProduct.name
            holder.binding.itemPrice.text = favoriteProduct.originPrice.toString()
        }
    }

}