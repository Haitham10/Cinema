package com.example.cinema.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CastDto(
    val id: Int,
    val name: String,
    val character: String = "",

    @SerialName("profile_path")
    val profilePath: String? = null
)