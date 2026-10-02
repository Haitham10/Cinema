package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.selector.TrailerSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MoviesRepository,
    private val trailerSelector: TrailerSelector
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
    private var trailerJob: Job? = null

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

        loadCast()
    }

    fun retryTrailer() {
        val currentState =
            _uiState.value as? MovieDetailsUiState.Success
                ?: return

        if (currentState.trailerState !is TrailerUiState.Error) return

        loadTrailer()
    }

    private fun loadMovie() {
        if (movieJob?.isActive == true) return

        castJob?.cancel()
        trailerJob?.cancel()

        _uiState.value = MovieDetailsUiState.Loading

        movieJob = viewModelScope.launch {
            try {
                val movie = repository.getMovieById(movieId)

                if (movie == null) {
                    _uiState.value = MovieDetailsUiState.NotFound
                    return@launch
                }

                _uiState.value = MovieDetailsUiState.Success(
                    movie = movie
                )

                loadCast()
                loadTrailer()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = MovieDetailsUiState.Error(
                    message = "Unable to load movie. Please try again."
                )
            }
        }
    }

    private fun loadCast() {
        if (castJob?.isActive == true) return

        updateCastState(CastUiState.Loading)

        castJob = viewModelScope.launch {
            try {
                val members = repository.getMovieCast(movieId)

                updateCastState(
                    CastUiState.Success(members)
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                updateCastState(
                    CastUiState.Error(
                        "Unable to load cast. Please try again."
                    )
                )
            }
        }
    }

    private fun loadTrailer() {
        if (trailerJob?.isActive == true) return

        updateTrailerState(TrailerUiState.Loading)

        trailerJob = viewModelScope.launch {
            try {
                val videos = repository.getMovieVideos(movieId)
                val trailer = trailerSelector.select(videos)

                val trailerState = if (trailer == null) {
                    TrailerUiState.Unavailable
                } else {
                    TrailerUiState.Success(trailer)
                }

                updateTrailerState(trailerState)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                updateTrailerState(
                    TrailerUiState.Error(
                        "Unable to load trailer. Please try again."
                    )
                )
            }
        }
    }

    private fun updateCastState(castState: CastUiState) {
        _uiState.update { currentState ->
            if (currentState is MovieDetailsUiState.Success) {
                currentState.copy(castState = castState)
            } else {
                currentState
            }
        }
    }

    private fun updateTrailerState(trailerState: TrailerUiState) {
        _uiState.update { currentState ->
            if (currentState is MovieDetailsUiState.Success) {
                currentState.copy(trailerState = trailerState)
            } else {
                currentState
            }
        }
    }
}