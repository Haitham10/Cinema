package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MoviesRepository
) : ViewModel() {

    private val movieId: Int =
        checkNotNull(savedStateHandle["movieId"])

    private val _uiState =
        MutableStateFlow<MovieDetailsUiState>(
            MovieDetailsUiState.Loading
        )

    val uiState: StateFlow<MovieDetailsUiState> =
        _uiState.asStateFlow()

    init {
        loadMovie()
    }

    private fun loadMovie() {
        viewModelScope.launch {
            val movie = repository.getMovieById(movieId)

            _uiState.value = if (movie == null) {
                MovieDetailsUiState.NotFound
            } else {
                MovieDetailsUiState.Success(movie)
            }
        }
    }
}