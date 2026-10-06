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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.withContext

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

    @Test
    fun init_genresFail_moviesStillDisplayed() = runTest {
        val repository = FakeMoviesRepository(
            movies = expectedMovies
        ).apply {
            genresError = IllegalStateException("Genres failed")
        }

        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(expectedMovies, state.movies)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)

        assertFalse(state.isGenresLoading)
        assertEquals(
            "Unable to load categories. Please try again.",
            state.genresErrorMessage
        )
    }

    @Test
    fun selectGenre_loadsGenreMovies_thenAllLoadsPopular() = runTest {
        val actionMovies = listOf(expectedMovies.first())

        val repository = FakeMoviesRepository(
            movies = expectedMovies
        ).apply {
            genreMovies = mapOf(28 to actionMovies)
        }

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        assertEquals(repository.genres, viewModel.uiState.value.genres)

        viewModel.selectGenre(28)

        assertEquals(28, viewModel.uiState.value.selectedGenreId)
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.movies.isEmpty())

        advanceUntilIdle()

        assertEquals(listOf(28), repository.requestedGenreIds)
        assertEquals(actionMovies, viewModel.uiState.value.movies)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.selectGenre(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedGenreId)
        assertEquals(expectedMovies, viewModel.uiState.value.movies)
        assertEquals(2, repository.getMoviesCallCount)
    }

    @Test
    fun retryGenres_afterFailure_doesNotReloadMovies() = runTest {
        val repository = FakeMoviesRepository(
            movies = expectedMovies
        ).apply {
            genresError = IllegalStateException("Genres failed")
        }

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        repository.genresError = null

        viewModel.retryGenres()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(repository.genres, state.genres)
        assertNull(state.genresErrorMessage)
        assertFalse(state.isGenresLoading)
        assertEquals(2, repository.getGenresCallCount)

        assertEquals(1, repository.getMoviesCallCount)
        assertEquals(expectedMovies, state.movies)
    }

    @Test
    fun selectSameGenre_doesNotRequestMoviesAgain() = runTest {
        val repository = FakeMoviesRepository(
            movies = expectedMovies
        ).apply {
            genreMovies = mapOf(
                28 to listOf(expectedMovies.first())
            )
        }

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.selectGenre(28)
        advanceUntilIdle()

        viewModel.selectGenre(28)
        advanceUntilIdle()

        assertEquals(listOf(28), repository.requestedGenreIds)
        assertEquals(28, viewModel.uiState.value.selectedGenreId)
    }

    private class FakeMoviesRepository(
        var movies: List<Movie> = emptyList(),
        var error: Exception? = null
    ) : MoviesRepository {

        var genreMoviesResponder:
                (suspend (Int) -> List<Movie>)? = null

        val cancelledGenreIds = mutableListOf<Int>()

        
        var genres: List<Genre> = listOf(
            Genre(id = 28, name = "Action"),
            Genre(id = 35, name = "Comedy")
        )

        var genresError: Exception? = null

        var getGenresCallCount = 0
            private set

        val requestedGenreIds = mutableListOf<Int>()

        var genreMovies: Map<Int, List<Movie>> = emptyMap()

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
            getGenresCallCount++

            genresError?.let { throw it }

            return genres
        }

        override suspend fun getMoviesByGenre(
            genreId: Int
        ): List<Movie> {
            requestedGenreIds.add(genreId)

            return try {
                genreMoviesResponder?.invoke(genreId)
                    ?: run {
                        error?.let { throw it }
                        genreMovies[genreId].orEmpty()
                    }
            } catch (exception: CancellationException) {
                cancelledGenreIds.add(genreId)
                throw exception
            }
        }

        override suspend fun getRandomMovies(): List<Movie> {
            error("getRandomMovies is not configured for this test")
        }


    }
}