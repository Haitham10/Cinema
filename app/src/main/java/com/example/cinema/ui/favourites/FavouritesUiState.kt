package com.example.cinema.ui.favourites

import com.example.cinema.domain.model.Movie

sealed interface FavouritesUiState {

    data object Loading : FavouritesUiState

    data class Success(
        val movies: List<Movie>
    ) : FavouritesUiState

    data class Error(
        val message: String
    ) : FavouritesUiState
}