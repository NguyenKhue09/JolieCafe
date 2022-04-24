package com.nt118.joliecafe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("fullname")
    val fullName: String,
    val email: String,
    val phone: String,
    val dob: String,
    val thumbnail: String,
    val token: String,
    val defaultAddress: String,
    val addresses: List<String>,
    val coins: Int,
    val scores: Int,
)
