package com.example.cinema.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MoviesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private var moviesJob: Job? = null
    private var genresJob: Job? = null

    private var moviesRequestVersion = 0L

    init {
        loadMovies()
        loadGenres()
    }

    fun selectGenre(genreId: Int?) {
        val currentState = _uiState.value

        if (genreId == currentState.selectedGenreId) return

        if (
            genreId != null &&
            currentState.genres.none { it.id == genreId }
        ) {
            return
        }

        _uiState.update { state ->
            state.copy(selectedGenreId = genreId)
        }

        loadMovies()
    }

    fun retry() {
        val currentState = _uiState.value

        if (currentState.isLoading) return
        if (currentState.errorMessage == null) return

        loadMovies()
    }

    fun retryGenres() {
        val currentState = _uiState.value

        if (currentState.isGenresLoading) return
        if (currentState.genresErrorMessage == null) return

        loadGenres()
    }

    private fun loadMovies() {
        moviesRequestVersion++
        val currentVersion = moviesRequestVersion

        moviesJob?.cancel()

        val genreId = _uiState.value.selectedGenreId

        _uiState.update { currentState ->
            currentState.copy(
                movies = emptyList(),
                isLoading = true,
                errorMessage = null
            )
        }

        moviesJob = viewModelScope.launch {
            try {
                val movies = if (genreId == null) {
                    repository.getMovies()
                } else {
                    repository.getMoviesByGenre(genreId)
                }

                ensureActive()

                if (currentVersion != moviesRequestVersion) {
                    return@launch
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        movies = movies,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                ensureActive()

                if (currentVersion != moviesRequestVersion) {
                    return@launch
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Unable to load movies"
                    )
                }
            }
        }
    }

    private fun loadGenres() {
        if (genresJob?.isActive == true) return

        _uiState.update { currentState ->
            currentState.copy(
                isGenresLoading = true,
                genresErrorMessage = null
            )
        }

        genresJob = viewModelScope.launch {
            try {
                val genres = repository.getGenres()

                ensureActive()

                _uiState.update { currentState ->
                    currentState.copy(
                        genres = genres,
                        isGenresLoading = false,
                        genresErrorMessage = null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                ensureActive()

                _uiState.update { currentState ->
                    currentState.copy(
                        isGenresLoading = false,
                        genresErrorMessage =
                            "Unable to load categories. Please try again."
                    )
                }
            }
        }
    }
}