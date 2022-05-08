package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val productId: String,
    val size: String,
    val quantity: Int,
    val price: Double,
    @SerialName("product_detail")
    val productDetail: Product
)
