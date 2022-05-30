package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CartItem(
    @SerialName("_id")
    val id: String,
    val productId: String,
    @SerialName("product_detail")
    val productDetail:Product,
    val size: String,
    var quantity: Int,
    val price: Double
)
