package com.example.cinema.ui.home

import com.example.cinema.domain.model.Movie

data class HomeUiState(
    val movies: List<Movie> = emptyList()
)