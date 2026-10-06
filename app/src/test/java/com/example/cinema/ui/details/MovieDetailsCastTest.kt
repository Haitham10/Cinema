package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.runTest
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.selector.TrailerSelector
import com.example.cinema.domain.usecase.GetMovieDetailsUseCase
import com.example.cinema.testing.FakeFavouritesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsCastTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5
    )

    private val cast = listOf(
        CastMember(
            id = 100,
            name = "Test actor",
            character = "Test character",
            profileUrl = null
        )
    )

    private fun createViewModel(
        repository: MoviesRepository
    ): MovieDetailsViewModel {
        val favouritesRepository = FakeFavouritesRepository()

        return MovieDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("movieId" to movie.id)
            ),
            repository = repository,
            trailerSelector = TrailerSelector(),
            favouritesRepository = favouritesRepository,
            getMovieDetailsUseCase = GetMovieDetailsUseCase(
                moviesRepository = repository,
                favouritesRepository = favouritesRepository
            )
        )
    }

    @Test
    fun castLoading_movieRemainsVisible_thenCastSucceeds() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeCastRepository(
            movie = movie,
            members = cast,
            castGate = gate
        )

        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Loading ,
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast) ,
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun castFails_movieRemainsVisibleWithCastError() = runTest {
        val repository = FakeCastRepository(
            movie = movie,
            members = cast,
            castError = IOException("Connection failed")
        )

        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Error(
                    "Unable to load cast. Please try again."
                ) ,
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun retryCast_afterFailure_doesNotReloadMovie() = runTest {
        val repository = FakeCastRepository(
            movie = movie,
            members = cast,
            castError = IOException("Connection failed")
        )

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.castError = null

        viewModel.retryCast()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Loading ,
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast) ,
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )
        assertEquals(1, repository.movieRequestCount)
        assertEquals(2, repository.castRequestCount)
    }

    private class FakeCastRepository(
        private val movie: Movie,
        private val members: List<CastMember>,
        var castError: Exception? = null,
        private val castGate: CompletableDeferred<Unit>? = null
    ) : MoviesRepository {

        var movieRequestCount = 0
            private set

        var castRequestCount = 0
            private set

        override suspend fun getMovies(): List<Movie> {
            return listOf(movie)
        }

        override suspend fun getMovieById(id: Int): Movie? {
            movieRequestCount++
            return movie.takeIf { it.id == id }
        }

        override suspend fun getMovieCast(
            movieId: Int
        ): List<CastMember> {
            castRequestCount++

            castGate?.await()
            castError?.let { throw it }

            return members
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