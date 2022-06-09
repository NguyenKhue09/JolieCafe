package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    val id: String?,
    val userInfo: String,
    val products: List<BillProduct>,
    val address: Address,
    val totalCost: Double,
    val discountCost: Double,
    val shippingFee: Double,
    val voucherApply: List<Voucher>,
    val scoreApply: Int,
    val paid: Boolean,
    var paymentMethod: String,
    val orderDate: String,
    val status: String,
    val orderId: String,
)
