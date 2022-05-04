package com.nt118.joliecafe.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.models.Product

object FavoriteProductComparator: DiffUtil.ItemCallback<FavoriteProduct>() {
    override fun areItemsTheSame(oldItem: FavoriteProduct, newItem: FavoriteProduct): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FavoriteProduct, newItem: FavoriteProduct): Boolean {
        return oldItem == newItem
    }
}