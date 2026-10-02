package com.example.cinema.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideosResponseDto(
    val results: List<VideoDto> = emptyList()
)