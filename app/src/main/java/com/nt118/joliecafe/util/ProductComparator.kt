package com.nt118.joliecafe.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafe.models.Product

object ProductComparator: DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}