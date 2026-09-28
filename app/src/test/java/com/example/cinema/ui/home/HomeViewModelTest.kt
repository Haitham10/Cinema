package com.example.cinema.ui.home

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ui state contains movies returned by repository`() = runTest {
        val expectedMovies = listOf(
            Movie(
                id = 1,
                title = "Movie One",
                rating = 8.0
            ),
            Movie(
                id = 2,
                title = "Movie Two",
                rating = 7.5
            )
        )

        val repository = FakeMoviesRepository(expectedMovies)
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            expectedMovies,
            viewModel.uiState.value.movies
        )
    }

    private class FakeMoviesRepository(
        private val movies: List<Movie>
    ) : MoviesRepository {

        override suspend fun getMovies(): List<Movie> {
            return movies
        }

        override suspend fun getMovieById(id: Int): Movie? {
            return movies.find { movie ->
                movie.id == id
            }
        }
    }
}