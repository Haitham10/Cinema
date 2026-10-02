package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.selector.TrailerSelector
import com.example.cinema.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsTrailerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5
    )

    private val trailer = MovieVideo(
        id = "video-1",
        name = "Official Trailer",
        videoKey = "test-key",
        site = "YouTube",
        type = "Trailer",
        isOfficial = true
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
        return MovieDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("movieId" to movie.id)
            ),
            repository = repository,
            trailerSelector = TrailerSelector()
        )
    }

    @Test
    fun videosContainTrailer_stateIsSuccess() = runTest {
        val repository = FakeTrailerRepository(
            movie = movie,
            members = cast,
            videos = listOf(trailer)
        )

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast),
                trailerState = TrailerUiState.Success(trailer)
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun noSuitableTrailer_stateIsUnavailable() = runTest {
        val repository = FakeTrailerRepository(
            movie = movie,
            members = cast,
            videos = listOf(
                trailer.copy(type = "Clip")
            )
        )

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast),
                trailerState = TrailerUiState.Unavailable
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun retryTrailer_afterError_reloadsOnlyVideos() = runTest {
        val repository = FakeTrailerRepository(
            movie = movie,
            members = cast,
            videos = listOf(trailer),
            videoError = IOException("Connection failed")
        )

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast),
                trailerState = TrailerUiState.Error(
                    "Unable to load trailer. Please try again."
                )
            ),
            viewModel.uiState.value
        )

        repository.videoError = null
        viewModel.retryTrailer()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast),
                trailerState = TrailerUiState.Loading
            ),
            viewModel.uiState.value
        )

        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Success(
                movie = movie,
                castState = CastUiState.Success(cast),
                trailerState = TrailerUiState.Success(trailer)
            ),
            viewModel.uiState.value
        )

        assertEquals(1, repository.movieRequests)
        assertEquals(1, repository.castRequests)
        assertEquals(2, repository.videoRequests)
    }

    @Test
    fun castAndTrailer_preserveEachOther_inEitherCompletionOrder() =
        runTest {
            for (castFinishesFirst in listOf(true, false)) {
                val castGate = CompletableDeferred<Unit>()
                val videoGate = CompletableDeferred<Unit>()

                val repository = FakeTrailerRepository(
                    movie = movie,
                    members = cast,
                    videos = listOf(trailer),
                    castGate = castGate,
                    videoGate = videoGate
                )

                val viewModel = createViewModel(repository)
                advanceUntilIdle()

                assertEquals(
                    MovieDetailsUiState.Success(
                        movie = movie,
                        castState = CastUiState.Loading,
                        trailerState = TrailerUiState.Loading
                    ),
                    viewModel.uiState.value
                )

                if (castFinishesFirst) {
                    castGate.complete(Unit)
                } else {
                    videoGate.complete(Unit)
                }

                advanceUntilIdle()

                assertEquals(
                    MovieDetailsUiState.Success(
                        movie = movie,
                        castState = if (castFinishesFirst) {
                            CastUiState.Success(cast)
                        } else {
                            CastUiState.Loading
                        },
                        trailerState = if (castFinishesFirst) {
                            TrailerUiState.Loading
                        } else {
                            TrailerUiState.Success(trailer)
                        }
                    ),
                    viewModel.uiState.value
                )

                if (castFinishesFirst) {
                    videoGate.complete(Unit)
                } else {
                    castGate.complete(Unit)
                }

                advanceUntilIdle()

                assertEquals(
                    MovieDetailsUiState.Success(
                        movie = movie,
                        castState = CastUiState.Success(cast),
                        trailerState = TrailerUiState.Success(trailer)
                    ),
                    viewModel.uiState.value
                )
            }
        }

    private class FakeTrailerRepository(
        private val movie: Movie,
        private val members: List<CastMember>,
        private val videos: List<MovieVideo>,
        var videoError: Exception? = null,
        private val castGate: CompletableDeferred<Unit>? = null,
        private val videoGate: CompletableDeferred<Unit>? = null
    ) : MoviesRepository {

        var movieRequests = 0
            private set

        var castRequests = 0
            private set

        var videoRequests = 0
            private set

        override suspend fun getMovies(): List<Movie> {
            return listOf(movie)
        }

        override suspend fun getMovieById(id: Int): Movie? {
            movieRequests++
            return movie.takeIf { it.id == id }
        }

        override suspend fun getMovieCast(
            movieId: Int
        ): List<CastMember> {
            castRequests++
            castGate?.await()
            return members
        }

        override suspend fun getMovieVideos(
            movieId: Int
        ): List<MovieVideo> {
            videoRequests++
            videoGate?.await()
            videoError?.let { throw it }
            return videos
        }
    }
}