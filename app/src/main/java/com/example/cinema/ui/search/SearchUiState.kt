package com.example.cinema.ui.search

import com.example.cinema.domain.model.Movie

data class SearchUiState(
    val query: String = "",
    val resultState: SearchResultState = SearchResultState.Idle
)

sealed interface SearchResultState {

    data object Idle : SearchResultState

    data object Loading : SearchResultState

    data class Success(
        val movies: List<Movie>
    ) : SearchResultState

    data object Empty : SearchResultState

    data class Error(
        val message: String
    ) : SearchResultState
}