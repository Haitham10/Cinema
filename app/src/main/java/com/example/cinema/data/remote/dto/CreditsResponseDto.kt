package com.example.cinema.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreditsResponseDto(
    val cast: List<CastDto> = emptyList()
)