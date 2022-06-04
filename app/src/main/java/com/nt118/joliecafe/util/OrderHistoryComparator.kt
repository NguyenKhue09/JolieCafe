package com.nt118.joliecafe.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.models.OrderHistory

object OrderHistoryComparator: DiffUtil.ItemCallback<OrderHistory>() {
    override fun areItemsTheSame(oldItem: OrderHistory, newItem: OrderHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: OrderHistory, newItem: OrderHistory): Boolean {
        return oldItem == newItem
    }
}