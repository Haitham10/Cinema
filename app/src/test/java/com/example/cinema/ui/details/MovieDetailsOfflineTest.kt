package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.selector.TrailerSelector
import com.example.cinema.domain.usecase.GetMovieDetailsUseCase
import com.example.cinema.testing.FakeFavouritesRepository
import com.example.cinema.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsOfflineTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun networkFails_savedMovieDetailsRemainVisible() = runTest {
        val savedMovie = Movie(
            id = 42,
            title = "Saved movie",
            rating = 8.5,
            overview = "Saved overview",
            releaseDate = "2026-01-15"
        )

        val favouritesRepository = FakeFavouritesRepository()
        favouritesRepository.save(savedMovie)

        val moviesRepository = OfflineMoviesRepository()

        val viewModel = MovieDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("movieId" to savedMovie.id)
            ),
            repository = moviesRepository,
            trailerSelector = TrailerSelector(),
            favouritesRepository = favouritesRepository,
            getMovieDetailsUseCase = GetMovieDetailsUseCase(
                moviesRepository = moviesRepository,
                favouritesRepository = favouritesRepository
            )
        )

        try {
            advanceUntilIdle()

            val state = viewModel.uiState.value

            assertTrue(state is MovieDetailsUiState.Success)
            val success = state as MovieDetailsUiState.Success

            assertEquals(savedMovie, success.movie)

            assertEquals(
                CastUiState.Error(
                    "Unable to load cast. Please try again."
                ),
                success.castState
            )

            assertEquals(
                TrailerUiState.Error(
                    "Unable to load trailer. Please try again."
                ),
                success.trailerState
            )

            assertEquals(
                FavouriteUiState(isFavourite = true),
                viewModel.favouriteUiState.value
            )
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }
}

private class OfflineMoviesRepository : MoviesRepository {

    override suspend fun getMovies(): List<Movie> {
        throw IOException("Offline")
    }

    override suspend fun getMovieById(id: Int): Movie? {
        throw IOException("Offline")
    }

    override suspend fun getMovieCast(movieId: Int): List<CastMember> {
        throw IOException("Offline")
    }

    override suspend fun getMovieVideos(movieId: Int): List<MovieVideo> {
        throw IOException("Offline")
    }
    override suspend fun searchMovies(query: String): List<Movie> {
        error("searchMovies is not configured for this test")
    }
    override suspend fun getGenres(): List<Genre> {
        error("getGenres is not configured for this test")
    }

    override suspend fun getMoviesByGenre(
        genreId: Int
    ): List<Movie> {
        error("getMoviesByGenre is not configured for this test")
    }
}