package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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

    private var movieJob: Job? = null
    private var castJob: Job? = null

    init {
        loadMovie()
    }

    fun retry() {
        loadMovie()
    }

    fun retryCast() {
        val currentState =
            _uiState.value as? MovieDetailsUiState.Success
                ?: return

        if (currentState.castState !is CastUiState.Error) return

        loadCast(currentState.movie)
    }

    private fun loadMovie() {
        if (movieJob?.isActive == true) return

        castJob?.cancel()

        _uiState.value = MovieDetailsUiState.Loading

        movieJob = viewModelScope.launch {
            try {
                val movie = repository.getMovieById(movieId)

                if (movie == null) {
                    _uiState.value = MovieDetailsUiState.NotFound
                    return@launch
                }

                loadCast(movie)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = MovieDetailsUiState.Error(
                    message = "Unable to load movie. Please try again."
                )
            }
        }
    }

    private fun loadCast(movie: Movie) {
        if (castJob?.isActive == true) return

        _uiState.value = MovieDetailsUiState.Success(
            movie = movie,
            castState = CastUiState.Loading
        )

        castJob = viewModelScope.launch {
            try {
                val members = repository.getMovieCast(movie.id)

                _uiState.value = MovieDetailsUiState.Success(
                    movie = movie,
                    castState = CastUiState.Success(members)
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = MovieDetailsUiState.Success(
                    movie = movie,
                    castState = CastUiState.Error(
                        message = "Unable to load cast. Please try again."
                    )
                )
            }
        }
    }
}