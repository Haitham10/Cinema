package com.example.cinema.ui.details

import com.example.cinema.domain.model.Movie

sealed interface MovieDetailsUiState {

    data object Loading : MovieDetailsUiState

    data class Success(
        val movie: Movie
    ) : MovieDetailsUiState

    data object NotFound : MovieDetailsUiState
}