package com.example.cinema.ui.home

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.MovieVideo

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expectedMovies = listOf(
        Movie(
            id = 1,
            title = "Interstellar",
            rating = 8.7
        ),
        Movie(
            id = 2,
            title = "Inception",
            rating = 8.8
        )
    )

    @Test
    fun init_initialStateIsLoading() = runTest {
        val repository = FakeMoviesRepository(
            movies = expectedMovies
        )

        val viewModel = HomeViewModel(repository)

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun init_repositorySucceeds_uiStateContainsMovies() = runTest {
        val repository = FakeMoviesRepository(
            movies = expectedMovies
        )

        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(expectedMovies, state.movies)
        assertNull(state.errorMessage)
    }

    @Test
    fun init_repositoryFails_uiStateContainsError() = runTest {
        val repository = FakeMoviesRepository(
            error = IllegalStateException("Network error")
        )

        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.movies.isEmpty())
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun retry_afterFailure_loadsMovies() = runTest {
        val repository = FakeMoviesRepository(
            error = IllegalStateException("Network error")
        )

        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        repository.error = null
        repository.movies = expectedMovies

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(2, repository.getMoviesCallCount)
        assertFalse(state.isLoading)
        assertEquals(expectedMovies, state.movies)
        assertNull(state.errorMessage)
    }

    private class FakeMoviesRepository(
        var movies: List<Movie> = emptyList(),
        var error: Exception? = null
    ) : MoviesRepository {

        var getMoviesCallCount: Int = 0
            private set

        override suspend fun getMovies(): List<Movie> {
            getMoviesCallCount++

            error?.let { exception ->
                throw exception
            }

            return movies
        }

        override suspend fun getMovieById(id: Int): Movie? {
            return movies.firstOrNull { movie ->
                movie.id == id
            }
        }

        override suspend fun getMovieCast(movieId: Int): List<CastMember> {
            return emptyList()
        }

        override suspend fun getMovieVideos(
            movieId: Int
        ): List<MovieVideo> {
            return emptyList()
        }

        override suspend fun searchMovies(query: String): List<Movie> {
            error("searchMovies is not configured for this test")
        }

        override suspend fun getGenres(): List<Genre> {
            error("getGenres is not configured for this test")
        }


    }
}