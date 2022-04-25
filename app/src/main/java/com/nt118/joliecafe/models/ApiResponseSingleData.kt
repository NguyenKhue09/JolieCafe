package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseSingleData<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
