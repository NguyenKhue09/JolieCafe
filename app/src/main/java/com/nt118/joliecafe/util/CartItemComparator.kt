package com.nt118.joliecafe.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafe.models.CartItem

object CartItemComparator : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}