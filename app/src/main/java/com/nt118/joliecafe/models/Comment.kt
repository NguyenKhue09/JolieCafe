package com.nt118.joliecafe.models

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val userThumbnail: String,
    val userName: String,
    val content: String,
    val rating: Int,
    val createdAt: String?
)
