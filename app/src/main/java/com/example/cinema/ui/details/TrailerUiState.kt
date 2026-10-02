package com.example.cinema.ui.details

import com.example.cinema.domain.model.MovieVideo

sealed interface TrailerUiState {

    data object Loading : TrailerUiState

    data class Success(
        val video: MovieVideo
    ) : TrailerUiState

    data object Unavailable : TrailerUiState

    data class Error(
        val message: String
    ) : TrailerUiState
}