package com.example.cinema.ui.home

import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie

data class HomeUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    val genres: List<Genre> = emptyList(),
    val isGenresLoading: Boolean = true,
    val genresErrorMessage: String? = null,
    val selectedGenreId: Int? = null,

    val randomMovies: List<Movie> = emptyList(),
    val isRandomMoviesLoading: Boolean = true,
    val randomMoviesErrorMessage: String? = null
)