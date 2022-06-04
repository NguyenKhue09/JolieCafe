package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class MomoPaymentRequestBody(
    val description: String,
    val customerNumber: String,
    val partnerRefId: String,
    val appData: String,
    val bill: Bill
)
