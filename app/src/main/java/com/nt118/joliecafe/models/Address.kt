package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val houseNumber: String,
    val street: String,
    val district: String,
    val province: String,
    val country: String
)
