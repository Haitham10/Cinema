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
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.selector.TrailerSelector

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
            repository = repository ,
            trailerSelector = TrailerSelector()
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = expectedMovie,
                castState = CastUiState.Success(emptyList()) ,
                trailerState = TrailerUiState.Unavailable
            ),
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
            repository = repository ,
            trailerSelector = TrailerSelector()
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
            repository = repository ,
            trailerSelector = TrailerSelector()
        )
    }

    @Test
    fun init_repositoryFails_uiStateIsError() = runTest {
        val repository = FakeMoviesRepository(
            movies = listOf(expectedMovie),
            error = java.io.IOException("Connection failed")
        )

        val viewModel = MovieDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("movieId" to expectedMovie.id)
            ),
            repository = repository ,
            trailerSelector = TrailerSelector()
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Error(
                "Unable to load movie. Please try again."
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun retry_afterFailure_uiStateIsSuccess() = runTest {
        val repository = FakeMoviesRepository(
            movies = listOf(expectedMovie),
            error = java.io.IOException("Connection failed")
        )

        val viewModel = MovieDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("movieId" to expectedMovie.id)
            ),
            repository = repository ,
            trailerSelector = TrailerSelector()
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Error(
                "Unable to load movie. Please try again."
            ),
            viewModel.uiState.value
        )

        repository.error = null

        viewModel.retry()

        assertEquals(
            MovieDetailsUiState.Loading,
            viewModel.uiState.value
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = expectedMovie,
                castState = CastUiState.Success(emptyList()) ,
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )

        assertEquals(2, repository.getMovieByIdCallCount)
    }

}

private class FakeMoviesRepository(
    private val movies: List<Movie>,
    var error: Exception? = null
) : MoviesRepository {

    var getMovieByIdCallCount: Int = 0
        private set

    override suspend fun getMovies(): List<Movie> {
        return movies
    }

    override suspend fun getMovieById(id: Int): Movie? {
        getMovieByIdCallCount++

        error?.let { throw it }

        return movies.find { movie ->
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
}