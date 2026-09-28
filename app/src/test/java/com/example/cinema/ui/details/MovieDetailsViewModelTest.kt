package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsViewModelTest {

    private val expectedMovie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5
    )
    private val repository = FakeMoviesRepository(
        movies = listOf(expectedMovie)
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_existingMovieId_uiStateIsSuccess() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf("movieId" to expectedMovie.id)
        )

        val viewModel = MovieDetailsViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(expectedMovie),
            viewModel.uiState.value
        )
    }

    @Test
    fun init_unknownMovieId_uiStateIsNotFound() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf("movieId" to 999)
        )

        val viewModel = MovieDetailsViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.NotFound,
            viewModel.uiState.value
        )
    }

    @Test(expected = IllegalStateException::class)
    fun init_missingMovieId_throwsIllegalStateException() {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val repository = FakeMoviesRepository(
            movies = listOf(expectedMovie)
        )

        // Act
        MovieDetailsViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository
        )
    }
}

private class FakeMoviesRepository(
    private val movies: List<Movie>
) : MoviesRepository {

    override suspend fun getMovies(): List<Movie>{
        return movies
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return movies.find { movie ->
            movie.id == id
        }
    }
}