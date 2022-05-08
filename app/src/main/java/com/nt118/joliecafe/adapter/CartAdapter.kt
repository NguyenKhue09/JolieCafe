package com.nt118.joliecafe.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemCartBinding
import com.nt118.joliecafe.models.CartItem
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.nt118.joliecafe.R

class CartAdapter(
    private val mActivity: Activity,
    diffCallback: DiffUtil.ItemCallback<CartItem>,
) : PagingDataAdapter<CartItem, CartAdapter.ViewHolder>(diffCallback) {

    class ViewHolder(var binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCartBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = getItem(position)
        cartItem?.let {
            holder.binding.ivThumbnail.load(cartItem.productDetail.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }
            holder.binding.tvProductName.text = cartItem.productDetail.name
            holder.binding.tvProductDescription.text = cartItem.productDetail.description
            holder.binding.tvAmount.text = cartItem.quantity.toString()
        }
    }
}