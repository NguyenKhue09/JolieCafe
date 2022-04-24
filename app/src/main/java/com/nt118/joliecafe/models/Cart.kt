package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class Cart(
    val product: Product,
    val size: String,
    val quantity: Int,
    val price: Double
)
