package com.example.cinema.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(
    val id: String,
    val name: String,
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean = false
)