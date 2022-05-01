package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val discountPercent: Int,
    val startDateDiscount: String,
    val endDateDiscount: String,
    @SerialName("_id")
    val id: String,
    val name: String,
    val status: String,
    val description: String,
    val thumbnail: String,
    val comments: List<String>,
    val originPrice: Double,
    val avgRating: Int,
    val isDeleted: Boolean,
    val type: String,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null
)
