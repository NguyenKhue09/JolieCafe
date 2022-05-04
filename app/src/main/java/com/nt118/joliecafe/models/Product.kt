package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val discountPercent: Int? = null,
    val startDateDiscount: String? = null,
    val endDateDiscount: String? = null,
    @SerialName("_id")
    val id: String,
    val name: String,
    val status: String,
    val description: String,
    val thumbnail: String,
    val comments: List<String>? = null,
    val originPrice: Double,
    val avgRating: Int,
    val isDeleted: Boolean,
    val type: String,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null
)
