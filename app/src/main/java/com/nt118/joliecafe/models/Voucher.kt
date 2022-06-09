package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Voucher(
    @SerialName("_id")
    val id: String,
    val code: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val type: String,
    val condition: Int,
    val discountPercent: Double,
    val quantity: Int
)
