package com.example.cinema.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MoviesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())

    val uiState: StateFlow<SearchUiState> =
        _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var searchVersion = 0L

    fun onQueryChange(query: String) {
        if (query == _uiState.value.query) return

        _uiState.update { currentState ->
            currentState.copy(query = query)
        }

        startSearch(debounce = true)
    }

    fun retry() {
        if (_uiState.value.resultState !is SearchResultState.Error) {
            return
        }

        startSearch(debounce = false)
    }

    private fun startSearch(debounce: Boolean) {
        searchVersion++
        val currentVersion = searchVersion

        searchJob?.cancel()

        val query = _uiState.value.query.trim()

        if (query.isBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    resultState = SearchResultState.Idle
                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                resultState = SearchResultState.Loading
            )
        }

        searchJob = viewModelScope.launch {
            try {
                if (debounce) {
                    delay(SEARCH_DEBOUNCE_MILLIS)
                }

                ensureActive()

                val movies = repository.searchMovies(query)

                ensureActive()

                if (currentVersion != searchVersion) {
                    return@launch
                }

                val resultState = if (movies.isEmpty()) {
                    SearchResultState.Empty
                } else {
                    SearchResultState.Success(movies)
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        resultState = resultState
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                ensureActive()

                if (currentVersion != searchVersion) {
                    return@launch
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        resultState = SearchResultState.Error(
                            message = "Unable to search movies. Please try again."
                        )
                    )
                }
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
    }
}