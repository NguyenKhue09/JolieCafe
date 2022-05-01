package com.nt118.joliecafe.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("_id")
    val id: String,
    @SerialName("fullname")
    val fullName: String,
    val email: String,
    val phone: String,
    val dob: String ? = null,
    val thumbnail: String ?= "",
    val token: String,
    val defaultAddress: String? = null,
    val coins: Int,
    val scores: Int,
    val disable: Boolean,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null
): java.io.Serializable
