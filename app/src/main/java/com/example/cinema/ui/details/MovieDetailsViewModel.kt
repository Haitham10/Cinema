package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.repository.FavouritesRepository
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.selector.TrailerSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MoviesRepository,
    private val trailerSelector: TrailerSelector,
    private val favouritesRepository: FavouritesRepository
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
    private val _favouriteUiState =
        MutableStateFlow(FavouriteUiState())

    val favouriteUiState: StateFlow<FavouriteUiState> =
        _favouriteUiState.asStateFlow()

    private var favouriteObservationJob: Job? = null

    init {
        loadMovie()
        observeFavourite()
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
    private fun observeFavourite() {
        favouriteObservationJob?.cancel()

        _favouriteUiState.update { current ->
            current.copy(
                isFavourite = null,
                errorMessage = null
            )
        }

        favouriteObservationJob = viewModelScope.launch {
            try {
                favouritesRepository.observeIsFavourite(movieId)
                    .collect { isFavourite ->
                        _favouriteUiState.update { current ->
                            current.copy(
                                isFavourite = isFavourite
                            )
                        }
                    }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _favouriteUiState.update { current ->
                    current.copy(
                        isFavourite = null,
                        errorMessage =
                            "Unable to read favourite status. Please try again."
                    )
                }
            }
        }
    }
    fun retryFavourite() {
        val current = _favouriteUiState.value

        if (current.isSaving) return
        if (current.isFavourite != null) return
        if (current.errorMessage == null) return

        observeFavourite()
    }

    fun toggleFavourite() {
        val details =
            _uiState.value as? MovieDetailsUiState.Success
                ?: return

        val favouriteState = _favouriteUiState.value
        val wasFavourite = favouriteState.isFavourite ?: return

        if (favouriteState.isSaving) return

        _favouriteUiState.update { current ->
            current.copy(
                isSaving = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                if (wasFavourite) {
                    favouritesRepository.deleteById(movieId)
                } else {
                    favouritesRepository.save(details.movie)
                }

                observeFavourite()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _favouriteUiState.update { current ->
                    current.copy(
                        errorMessage =
                            "Unable to update favourite. Please try again."
                    )
                }
            } finally {
                _favouriteUiState.update { current ->
                    current.copy(isSaving = false)
                }
            }
        }
    }
}