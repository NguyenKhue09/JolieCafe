package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class SuspendUserMoneyResponse(
    val status: Int,
    val message: String,
    val amount: Long,
    val transid: String,
    val signature: String
)
