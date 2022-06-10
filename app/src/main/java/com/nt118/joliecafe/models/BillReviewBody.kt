package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class BillReviewBody(
    val billId: String,
    val productIds: List<String>,
    val content: String,
    val rating: Float
)