package com.example.cinema.domain.usecase

import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.repository.FavouritesRepository
import com.example.cinema.domain.repository.MoviesRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GetMovieDetailsUseCaseTest {

    private val remoteMovie = Movie(
        id = 42,
        title = "Remote movie",
        rating = 8.5
    )

    private val savedMovie = remoteMovie.copy(
        title = "Saved movie",
        rating = 8.0
    )

    @Test
    fun remoteSucceeds_returnsRemoteMovieWithoutLocalRead() = runTest {
        val remote = DetailsMoviesRepository(
            result = remoteMovie
        )
        val local = DetailsFavouritesRepository(
            result = savedMovie
        )
        val useCase = GetMovieDetailsUseCase(remote, local)

        val result = useCase(42)

        assertEquals(remoteMovie, result)
        assertEquals(listOf(42), remote.requestedIds)
        assertEquals(emptyList<Int>(), local.requestedIds)
    }

    @Test
    fun networkFails_savedMovieExists_returnsSavedMovie() = runTest {
        val remote = DetailsMoviesRepository(
            failure = IOException("Connection failed")
        )
        val local = DetailsFavouritesRepository(
            result = savedMovie
        )
        val useCase = GetMovieDetailsUseCase(remote, local)

        val result = useCase(42)

        assertEquals(savedMovie, result)
        assertEquals(listOf(42), remote.requestedIds)
        assertEquals(listOf(42), local.requestedIds)
    }

    @Test
    fun networkFails_noSavedMovie_throwsOriginalException() = runTest {
        val networkError = IOException("Connection failed")
        val remote = DetailsMoviesRepository(
            failure = networkError
        )
        val local = DetailsFavouritesRepository()
        val useCase = GetMovieDetailsUseCase(remote, local)

        assertFailsWithSame(networkError) {
            useCase(42)
        }

        assertEquals(listOf(42), local.requestedIds)
    }

    @Test
    fun remoteReturnsNull_returnsNullWithoutLocalRead() = runTest {
        val remote = DetailsMoviesRepository()
        val local = DetailsFavouritesRepository(
            result = savedMovie
        )
        val useCase = GetMovieDetailsUseCase(remote, local)

        val result = useCase(42)

        assertNull(result)
        assertEquals(listOf(42), remote.requestedIds)
        assertEquals(emptyList<Int>(), local.requestedIds)
    }

    @Test
    fun httpFails_propagatesExceptionWithoutLocalRead() = runTest {
        for (statusCode in listOf(401, 404)) {
            val httpError = HttpException(
                Response.error<Movie>(
                    statusCode,
                    "{}".toResponseBody()
                )
            )

            val remote = DetailsMoviesRepository(
                failure = httpError
            )
            val local = DetailsFavouritesRepository(
                result = savedMovie
            )
            val useCase = GetMovieDetailsUseCase(remote, local)

            assertFailsWithSame(httpError) {
                useCase(42)
            }

            assertEquals(emptyList<Int>(), local.requestedIds)
        }
    }

    @Test
    fun remoteCancelled_propagatesCancellationWithoutLocalRead() = runTest {
        val cancellation = CancellationException("Cancelled")
        val remote = DetailsMoviesRepository(
            failure = cancellation
        )
        val local = DetailsFavouritesRepository(
            result = savedMovie
        )
        val useCase = GetMovieDetailsUseCase(remote, local)

        assertFailsWithSame(cancellation) {
            useCase(42)
        }

        assertEquals(emptyList<Int>(), local.requestedIds)
    }

    @Test
    fun localReadFails_propagatesLocalException() = runTest {
        val localError = IllegalStateException("Database read failed")
        val remote = DetailsMoviesRepository(
            failure = IOException("Connection failed")
        )
        val local = DetailsFavouritesRepository(
            failure = localError
        )
        val useCase = GetMovieDetailsUseCase(remote, local)

        assertFailsWithSame(localError) {
            useCase(42)
        }

        assertEquals(listOf(42), local.requestedIds)
    }

    private suspend fun assertFailsWithSame(
        expected: Exception,
        block: suspend () -> Unit
    ) {
        try {
            block()
        } catch (actual: Exception) {
            assertSame(expected, actual)
            return
        }

        fail("Expected the operation to throw an exception")
    }
}

private class DetailsMoviesRepository(
    private val result: Movie? = null,
    private val failure: Exception? = null
) : MoviesRepository {

    val requestedIds = mutableListOf<Int>()

    override suspend fun getMovieById(id: Int): Movie? {
        requestedIds.add(id)
        failure?.let { throw it }
        return result
    }

    override suspend fun getMovies(): List<Movie> {
        error("Not used by GetMovieDetailsUseCase")
    }

    override suspend fun getMovieCast(movieId: Int): List<CastMember> {
        error("Not used by GetMovieDetailsUseCase")
    }

    override suspend fun getMovieVideos(movieId: Int): List<MovieVideo> {
        error("Not used by GetMovieDetailsUseCase")
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

private class DetailsFavouritesRepository(
    private val result: Movie? = null,
    private val failure: Exception? = null
) : FavouritesRepository {

    val requestedIds = mutableListOf<Int>()

    override suspend fun getById(movieId: Int): Movie? {
        requestedIds.add(movieId)
        failure?.let { throw it }
        return result
    }

    override fun observeFavourites(): Flow<List<Movie>> {
        error("Not used by GetMovieDetailsUseCase")
    }

    override fun observeIsFavourite(movieId: Int): Flow<Boolean> {
        error("Not used by GetMovieDetailsUseCase")
    }

    override suspend fun save(movie: Movie) {
        error("Not used by GetMovieDetailsUseCase")
    }

    override suspend fun deleteById(movieId: Int) {
        error("Not used by GetMovieDetailsUseCase")
    }
}