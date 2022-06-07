package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @SerialName("_id")
    val id: String,
    val title: String,
    val message: String,
    val image: String? = null,
    val type: String,
    val productId: String? = null,
    val productName: String? = null,
    val voucherId: String? = null,
    val voucherCode: String? = null,
    val billId: String? = null,
    val userId: String? = null,
    val createdAt: String,
    val updatedAt: String
)
