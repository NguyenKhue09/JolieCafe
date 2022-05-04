package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteProduct(
    @SerialName("_id")
    val id: String,
    val product: Product
)
