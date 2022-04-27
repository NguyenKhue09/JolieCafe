package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("_id")
    val id: String,
    val name: String,
    val status: String,
    val type: String,
    val description: String,
    @SerialName("thumnail")
    val thumbnail: String,
    val originPrice: Double,
    val discountPercent: Int,
    val avgRating: Int,
    val isDeleted: Boolean,
    val startDateDiscount: String,
    val endDateDiscount: String
)
