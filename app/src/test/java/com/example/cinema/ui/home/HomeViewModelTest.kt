package com.example.cinema.ui.home

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun init_repositoryHasMovies_uiStateContainsMovies() {
        // Arrange
        val expectedMovies = listOf(
            Movie(
                id = 10,
                title = "Test movie",
                rating = 8.0
            ),
            Movie(
                id = 42,
                title = "Another movie",
                rating = 7.5
            )
        )

        val repository = FakeMoviesRepository(expectedMovies)

        // Act
        val viewModel = HomeViewModel(repository)

        // Assert
        assertEquals(
            expectedMovies,
            viewModel.uiState.value.movies
        )
    }

    @Test
    fun init_repositoryIsEmpty_uiStateContainsEmptyList() {
        // Arrange
        val repository = FakeMoviesRepository(emptyList())

        // Act
        val viewModel = HomeViewModel(repository)

        // Assert
        assertTrue(
            viewModel.uiState.value.movies.isEmpty()
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