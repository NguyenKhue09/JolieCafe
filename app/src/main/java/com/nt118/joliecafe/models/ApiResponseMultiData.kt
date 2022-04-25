package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseMultiData<T>(
    val success: Boolean,
    val message: String,
    val data: List<T> = emptyList()
)
