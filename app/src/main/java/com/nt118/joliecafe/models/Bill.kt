package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    val id: String,
    val userInfo: User,
    val products: List<BillProduct>,
    val address: Address,
    val phone: String,
    val totalCost: Double,
    val discountCost: Double,
    val shippingFee: Double,
    val voucherApply: List<Voucher>,
    val scoreApply: Int,
    val paid: Boolean,
    val received: Boolean,
    val paymentMethod: String,
    val orderDate: String,
    val isRated: Boolean,
    val status: String
)
