package com.example.cinema.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesResponseDto(
    val page: Int = 1,
    val results: List<MovieDto> = emptyList(),

    @SerialName("total_pages")
    val totalPages: Int = 0,

    @SerialName("total_results")
    val totalResults: Int = 0
)