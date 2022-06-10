package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderHistory(
    @SerialName("_id")
    val id: String,
    val address: OrderHistoryUserInfos,
    val totalCost: Double,
    val discountCost: Double,
    val shippingFee: Double,
    val scoreApply: Int,
    val paid: Boolean,
    val paymentMethod: String,
    val orderDate: String,
    val status: String,
    val orderId: String,
    val products: List<OrderHistoryProduct>,
    val isRated: Boolean = false,
)
