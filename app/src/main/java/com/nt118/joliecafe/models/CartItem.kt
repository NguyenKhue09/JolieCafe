package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CartItem(
    @SerialName("_id")
    val id: String,
    val productId: Product,
    val size: String,
    var quantity: Int,
    val price: Double
)
