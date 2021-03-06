package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class BillProduct(
    val productId: String,
    val size: String,
    val quantity: Int,
    val price: Double
)
