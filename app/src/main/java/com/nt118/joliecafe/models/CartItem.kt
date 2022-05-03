package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val productId: String,
    val name: String,
    val description: String,
    val thumbnail: String,
    val avgRating: Int,
    val size: String,
    val quantity: Int,
    val price: Double,
    val type: String,
)
