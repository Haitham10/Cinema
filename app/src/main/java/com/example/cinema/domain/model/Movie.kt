package com.example.cinema.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val rating: Double,
    val overview: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val releaseDate: String = "",
    val genreIds: List<Int> = emptyList(),
    val localPosterPath: String? = null,
    val localBackdropPath: String? = null
)