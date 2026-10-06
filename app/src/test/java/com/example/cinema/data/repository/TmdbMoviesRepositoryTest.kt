package com.example.cinema.data.repository

import com.example.cinema.data.remote.api.TmdbApiService
import com.example.cinema.data.remote.dto.CastDto
import com.example.cinema.data.remote.dto.CreditsResponseDto
import com.example.cinema.data.remote.dto.GenreDto
import com.example.cinema.data.remote.dto.GenresResponseDto
import com.example.cinema.data.remote.dto.MovieDto
import com.example.cinema.data.remote.dto.MoviesResponseDto
import com.example.cinema.data.remote.dto.VideoDto
import com.example.cinema.data.remote.dto.VideosResponseDto
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.Assert.assertTrue

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
            CreditsResponseDto(),
        private val videosResponse: VideosResponseDto =
            VideosResponseDto() ,
        private val discoverResponse: MoviesResponseDto = MoviesResponseDto(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )
    ) : TmdbApiService {
        var searchResponse = MoviesResponseDto(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )

        var searchError: Exception? = null

        var searchRequestCount = 0
            private set

        var requestedQuery: String? = null
            private set

        var requestedSearchLanguage: String? = null
            private set

        var requestedSearchPage: Int? = null
            private set

        var requestedIncludeAdult: Boolean? = null
            private set

        var popularRequestCount: Int = 0
            private set

        var requestedMovieId: Int? = null
            private set

        var requestedCastMovieId: Int? = null
            private set

        var requestedVideosMovieId: Int? = null
            private set

        var genresResponse = GenresResponseDto(
            genres = emptyList()
        )

        var genresError: Exception? = null

        var genresRequestCount = 0
            private set

        var requestedGenresLanguage: String? = null
            private set

        var genreMoviesResponse = MoviesResponseDto(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )

        var genreMoviesError: Exception? = null

        var genreMoviesRequestCount = 0
            private set

        var requestedGenreId: Int? = null
            private set

        var requestedGenreMoviesLanguage: String? = null
            private set

        var requestedGenreMoviesPage: Int? = null
            private set

        var requestedGenreMoviesSort: String? = null
            private set

        var requestedGenreMoviesIncludeAdult: Boolean? = null
            private set

        var requestedDiscoverPage: Int? = null
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

        override suspend fun getMovieVideos(
            movieId: Int,
            language: String
        ): VideosResponseDto {
            requestedVideosMovieId = movieId
            return videosResponse
        }
        override suspend fun searchMovies(
            query: String,
            language: String,
            page: Int,
            includeAdult: Boolean
        ): MoviesResponseDto {
            searchRequestCount++
            requestedQuery = query
            requestedSearchLanguage = language
            requestedSearchPage = page
            requestedIncludeAdult = includeAdult

            searchError?.let { throw it }

            return searchResponse
        }

        override suspend fun getMovieGenres(
            language: String
        ): GenresResponseDto {
            genresRequestCount++
            requestedGenresLanguage = language

            genresError?.let { throw it }

            return genresResponse
        }

        override suspend fun getMoviesByGenre(
            genreId: Int,
            language: String,
            page: Int,
            sortBy: String,
            includeAdult: Boolean
        ): MoviesResponseDto {
            genreMoviesRequestCount++
            requestedGenreId = genreId
            requestedGenreMoviesLanguage = language
            requestedGenreMoviesPage = page
            requestedGenreMoviesSort = sortBy
            requestedGenreMoviesIncludeAdult = includeAdult

            genreMoviesError?.let { throw it }

            return genreMoviesResponse
        }
        override suspend fun getDiscoverMovies(
            page: Int,
            language: String,
            sortBy: String,
            includeAdult: Boolean
        ): MoviesResponseDto {
            requestedDiscoverPage = page
            return discoverResponse
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
    @Test
    fun getMovieVideos_passesMovieIdAndReturnsMappedVideos() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto,
            videosResponse = VideosResponseDto(
                results = listOf(
                    VideoDto(
                        id = "tmdb-video-1",
                        name = "Official Trailer",
                        key = "youtube-video-key",
                        site = "YouTube",
                        type = "Trailer",
                        official = true
                    )
                )
            )
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val videos = repository.getMovieVideos(42)

        assertEquals(42, fakeApi.requestedVideosMovieId)
        assertEquals(
            listOf(
                MovieVideo(
                    id = "tmdb-video-1",
                    name = "Official Trailer",
                    videoKey = "youtube-video-key",
                    site = "YouTube",
                    type = "Trailer",
                    isOfficial = true
                )
            ),
            videos
        )
    }

    @Test
    fun getMovieVideos_emptyResponse_returnsEmptyList() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto,
            videosResponse = VideosResponseDto(
                results = emptyList()
            )
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val videos = repository.getMovieVideos(42)

        assertEquals(emptyList<MovieVideo>(), videos)
    }
    private fun createSearchApi(): FakeTmdbApiService {
        return FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto
        )
    }

    @Test
    fun searchMovies_trimsQuery_andReturnsMappedResults() = runTest {
        val fakeApi = createSearchApi().apply {
            searchResponse = MoviesResponseDto(
                page = 1,
                results = listOf(movieDto),
                totalPages = 1,
                totalResults = 1
            )
        }

        val repository = TmdbMoviesRepository(fakeApi)

        val result = repository.searchMovies("  Test movie  ")

        assertEquals(1, fakeApi.searchRequestCount)
        assertEquals("Test movie", fakeApi.requestedQuery)
        assertEquals("en-US", fakeApi.requestedSearchLanguage)
        assertEquals(1, fakeApi.requestedSearchPage)
        assertEquals(false, fakeApi.requestedIncludeAdult)

        assertEquals(1, result.size)

        val actual = result.single()

        assertEquals(movieDto.id, actual.id)
        assertEquals(movieDto.title, actual.title)
        assertEquals(movieDto.overview, actual.overview)
        assertEquals(movieDto.voteAverage, actual.rating, 0.0001)
        assertEquals(movieDto.releaseDate, actual.releaseDate)
        assertEquals(movieDto.genreIds, actual.genreIds)
        assertEquals(
            "https://image.tmdb.org/t/p/w500/poster.jpg",
            actual.posterUrl
        )
    }

    @Test
    fun searchMovies_blankQuery_doesNotCallApi() = runTest {
        val fakeApi = createSearchApi()
        val repository = TmdbMoviesRepository(fakeApi)

        for (query in listOf("", "   ", "\n\t")) {
            val result = repository.searchMovies(query)

            assertEquals(emptyList<Movie>(), result)
        }

        assertEquals(0, fakeApi.searchRequestCount)
    }

    @Test
    fun searchMovies_emptyResponse_returnsEmptyList() = runTest {
        val fakeApi = createSearchApi()
        val repository = TmdbMoviesRepository(fakeApi)

        val result = repository.searchMovies("Unknown movie")

        assertEquals(emptyList<Movie>(), result)
        assertEquals(1, fakeApi.searchRequestCount)
        assertEquals("Unknown movie", fakeApi.requestedQuery)
    }

    @Test
    fun searchMovies_apiFails_propagatesException() = runTest {
        val fakeApi = createSearchApi().apply {
            searchError = IOException("Connection failed")
        }

        val repository = TmdbMoviesRepository(fakeApi)

        try {
            repository.searchMovies("Batman")
            fail("Expected IOException")
        } catch (exception: IOException) {
            assertEquals("Connection failed", exception.message)
        }

        assertEquals(1, fakeApi.searchRequestCount)
    }

    private fun createGenresApi(): FakeTmdbApiService {
        return FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto
        )
    }

    @Test
    fun getGenres_returnsMappedGenres() = runTest {
        val fakeApi = createGenresApi().apply {
            genresResponse = GenresResponseDto(
                genres = listOf(
                    GenreDto(id = 28, name = "Action"),
                    GenreDto(id = 35, name = "Comedy")
                )
            )
        }

        val repository = TmdbMoviesRepository(fakeApi)

        val result = repository.getGenres()

        assertEquals(
            listOf(
                Genre(id = 28, name = "Action"),
                Genre(id = 35, name = "Comedy")
            ),
            result
        )
        assertEquals(1, fakeApi.genresRequestCount)
        assertEquals("en-US", fakeApi.requestedGenresLanguage)
    }

    @Test
    fun getGenres_emptyResponse_returnsEmptyList() = runTest {
        val fakeApi = createGenresApi()
        val repository = TmdbMoviesRepository(fakeApi)

        val result = repository.getGenres()

        assertEquals(emptyList<Genre>(), result)
        assertEquals(1, fakeApi.genresRequestCount)
    }

    @Test
    fun getGenres_apiFails_propagatesException() = runTest {
        val fakeApi = createGenresApi().apply {
            genresError = IOException("Connection failed")
        }

        val repository = TmdbMoviesRepository(fakeApi)

        try {
            repository.getGenres()
            fail("Expected IOException")
        } catch (exception: IOException) {
            assertEquals("Connection failed", exception.message)
        }

        assertEquals(1, fakeApi.genresRequestCount)
    }

    @Test
    fun getMoviesByGenre_passesGenreAndReturnsMappedMovies() = runTest {
        val fakeApi = createGenresApi().apply {
            genreMoviesResponse = MoviesResponseDto(
                page = 1,
                results = listOf(movieDto),
                totalPages = 1,
                totalResults = 1
            )
        }

        val repository = TmdbMoviesRepository(fakeApi)

        val result = repository.getMoviesByGenre(28)

        assertEquals(1, fakeApi.genreMoviesRequestCount)
        assertEquals(28, fakeApi.requestedGenreId)
        assertEquals("en-US", fakeApi.requestedGenreMoviesLanguage)
        assertEquals(1, fakeApi.requestedGenreMoviesPage)
        assertEquals(
            "popularity.desc",
            fakeApi.requestedGenreMoviesSort
        )
        assertEquals(false, fakeApi.requestedGenreMoviesIncludeAdult)

        assertEquals(1, result.size)

        val actual = result.single()

        assertEquals(movieDto.id, actual.id)
        assertEquals(movieDto.title, actual.title)
        assertEquals(movieDto.genreIds, actual.genreIds)
        assertEquals(movieDto.voteAverage, actual.rating, 0.0001)
        assertEquals(
            "https://image.tmdb.org/t/p/w500/poster.jpg",
            actual.posterUrl
        )
    }

    @Test
    fun getMoviesByGenre_emptyResponse_returnsEmptyList() = runTest {
        val fakeApi = createGenresApi()
        val repository = TmdbMoviesRepository(fakeApi)

        val result = repository.getMoviesByGenre(28)

        assertEquals(emptyList<Movie>(), result)
        assertEquals(1, fakeApi.genreMoviesRequestCount)
    }

    @Test
    fun getMoviesByGenre_apiFails_propagatesException() = runTest {
        val fakeApi = createGenresApi().apply {
            genreMoviesError = IOException("Connection failed")
        }

        val repository = TmdbMoviesRepository(fakeApi)

        try {
            repository.getMoviesByGenre(28)
            fail("Expected IOException")
        } catch (exception: IOException) {
            assertEquals("Connection failed", exception.message)
        }

        assertEquals(1, fakeApi.genreMoviesRequestCount)
    }

    @Test
    fun getRandomMovies_usesSupportedRandomPageAndReturnsMappedMovies() = runTest {
        val fakeApi = FakeTmdbApiService(
            popularResponse = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            ),
            detailsResponse = movieDto,
            discoverResponse = MoviesResponseDto(
                page = 1,
                results = listOf(movieDto),
                totalPages = 500,
                totalResults = 10_000
            )
        )

        val repository = TmdbMoviesRepository(fakeApi)

        val movies = repository.getRandomMovies()

        val requestedPage = checkNotNull(
            fakeApi.requestedDiscoverPage
        )

        assertTrue(requestedPage in 1..500)
        assertEquals(1, movies.size)
        assertEquals(42, movies.first().id)
        assertEquals("Test movie", movies.first().title)
    }
}
