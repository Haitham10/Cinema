package com.example.cinema.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenresResponseDto(
    val genres: List<GenreDto>
)