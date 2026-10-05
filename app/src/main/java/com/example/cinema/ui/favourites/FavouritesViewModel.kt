package com.example.cinema.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.repository.FavouritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: FavouritesRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<FavouritesUiState>(
            FavouritesUiState.Loading
        )

    val uiState: StateFlow<FavouritesUiState> =
        _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeFavourites()
    }

    fun retry() {
        if (_uiState.value !is FavouritesUiState.Error) return

        observeFavourites()
    }

    private fun observeFavourites() {
        observationJob?.cancel()

        _uiState.value = FavouritesUiState.Loading

        observationJob = viewModelScope.launch {
            try {
                repository.observeFavourites()
                    .collect { movies ->
                        _uiState.value =
                            FavouritesUiState.Success(
                                movies = movies
                            )
                    }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = FavouritesUiState.Error(
                    message =
                        "Unable to load favourites. Please try again."
                )
            }
        }
    }
}