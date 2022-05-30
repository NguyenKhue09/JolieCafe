package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItemByCategory(
    val type: String,
    val products: List<CartItem>
)