package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieDetailsViewModelTest {

    private val expectedMovie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5
    )

    @Test
    fun init_existingMovieId_uiStateIsSuccess() {
        // Arrange
        val savedStateHandle = SavedStateHandle(
            mapOf("movieId" to expectedMovie.id)
        )

        val repository = FakeMoviesRepository(
            movies = listOf(expectedMovie)
        )

        // Act
        val viewModel = MovieDetailsViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository
        )

        // Assert
        assertEquals(
            MovieDetailsUiState.Success(expectedMovie),
            viewModel.uiState.value
        )
    }

    @Test
    fun init_unknownMovieId_uiStateIsNotFound() {
        // Arrange
        val savedStateHandle = SavedStateHandle(
            mapOf("movieId" to 999)
        )

        val repository = FakeMoviesRepository(
            movies = listOf(expectedMovie)
        )

        // Act
        val viewModel = MovieDetailsViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository
        )

        // Assert
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

    override fun getMovies(): List<Movie> {
        return movies
    }

    override fun getMovieById(id: Int): Movie? {
        return movies.find { movie ->
            movie.id == id
        }
    }
}