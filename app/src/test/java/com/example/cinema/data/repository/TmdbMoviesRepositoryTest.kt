package com.example.cinema.data.repository

import com.example.cinema.data.remote.api.TmdbApiService
import com.example.cinema.data.remote.dto.CreditsResponseDto
import com.example.cinema.data.remote.dto.MovieDto
import com.example.cinema.data.remote.dto.MoviesResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import com.example.cinema.data.remote.dto.CastDto
import com.example.cinema.domain.model.CastMember

class TmdbMoviesRepositoryTest {

    private val movieDto = MovieDto(
        id = 42,
        title = "Test movie",
        overview = "Test overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        voteAverage = 8.5,
        releaseDate = "2026-09-28",
        genreIds = listOf(28, 12)
    )

    @Test
    fun getMovies_returnsMappedPopularMovies() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = listOf(movieDto),
                totalPages = 1,
                totalResults = 1
            ),
            detailsResponse = movieDto
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val movies = repository.getMovies()

        assertEquals(1, fakeApi.popularRequestCount)
        assertEquals(1, movies.size)
        assertEquals(42, movies.first().id)
        assertEquals("Test movie", movies.first().title)
        assertEquals(
            "https://image.tmdb.org/t/p/w500/poster.jpg",
            movies.first().posterUrl
        )
    }

    @Test
    fun getMovieById_passesIdAndReturnsMappedMovie() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val movie = repository.getMovieById(42)

        assertEquals(42, fakeApi.requestedMovieId)
        assertNotNull(movie)
        assertEquals(42, movie?.id)
        assertEquals("Test movie", movie?.title)
        assertEquals(8.5, movie?.rating)
    }

    private class FakeTmdbApiService(
        private val popularResponse: MoviesResponseDto,
        private val detailsResponse: MovieDto,
        private val creditsResponse: CreditsResponseDto =
            CreditsResponseDto()
    ) : TmdbApiService {

        var popularRequestCount: Int = 0
            private set

        var requestedMovieId: Int? = null
            private set

        var requestedCastMovieId: Int? = null
            private set

        override suspend fun getPopularMovies(
            language: String,
            page: Int
        ): MoviesResponseDto {
            popularRequestCount++
            return popularResponse
        }

        override suspend fun getMovieDetails(
            movieId: Int,
            language: String
        ): MovieDto {
            requestedMovieId = movieId
            return detailsResponse
        }
        override suspend fun getMovieCredits(
            movieId: Int,
            language: String
        ): CreditsResponseDto {
            requestedCastMovieId = movieId
            return creditsResponse
        }
    }

    @Test
    fun getMovieCast_passesMovieIdAndReturnsMappedCast() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto,
            creditsResponse = CreditsResponseDto(
                cast = listOf(
                    CastDto(
                        id = 100,
                        name = "Test actor",
                        character = "Test character",
                        profilePath = "/actor.jpg"
                    )
                )
            )
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val cast = repository.getMovieCast(42)

        assertEquals(42, fakeApi.requestedCastMovieId)
        assertEquals(
            listOf(
                CastMember(
                    id = 100,
                    name = "Test actor",
                    character = "Test character",
                    profileUrl =
                        "https://image.tmdb.org/t/p/w185/actor.jpg"
                )
            ),
            cast
        )
    }

    @Test
    fun getMovieCast_emptyResponse_returnsEmptyList() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto,
            creditsResponse = CreditsResponseDto(
                cast = emptyList()
            )
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val cast = repository.getMovieCast(42)

        assertEquals(emptyList<CastMember>(), cast)
    }
}