package com.example.cinema.data.repository

import com.example.cinema.data.local.dao.FavouriteMoviesDao
import com.example.cinema.data.local.entity.FavouriteMovieEntity
import com.example.cinema.domain.model.Movie
import java.io.IOException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomFavouritesRepositoryTest {

    private val movie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5,
        overview = "Test overview",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2026-01-15",
        genreIds = listOf(28, 12)
    )

    private val entity = FavouriteMovieEntity(
        id = 42,
        title = "Test movie",
        rating = 8.5,
        overview = "Test overview",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2026-01-15",
        genreIdsJson = "[28,12]",
        savedAt = 1_000L
    )

    @Test
    fun getById_existingMovie_returnsDomainMovie() = runTest {
        val dao = FakeFavouriteMoviesDao()
        dao.movieResult = entity
        val repository = RoomFavouritesRepository(dao)

        val result = repository.getById(42)

        assertEquals(movie, result)
        assertEquals(42, dao.requestedMovieId)
    }

    @Test
    fun getById_missingMovie_returnsNull() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(dao)

        val result = repository.getById(999)

        assertNull(result)
        assertEquals(999, dao.requestedMovieId)
    }

    @Test
    fun save_passesMovieDataAndCurrentTimeToDao() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(dao)

        val beforeSave = System.currentTimeMillis()

        repository.save(movie)

        val afterSave = System.currentTimeMillis()
        val saved = dao.savedMovie

        assertNotNull(saved)
        val actual = checkNotNull(saved)

        assertEquals(
            entity.copy(savedAt = actual.savedAt),
            actual
        )
        assertTrue(actual.savedAt in beforeSave..afterSave)
    }

    @Test
    fun deleteById_passesCorrectIdToDao() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(dao)

        repository.deleteById(42)

        assertEquals(42, dao.deletedMovieId)
    }

    @Test
    fun observeFavourites_mapsUpdatesToDomainMovies() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(dao)
        val emissions = Channel<List<Movie>>(Channel.UNLIMITED)

        backgroundScope.launch {
            repository.observeFavourites().collect { movies ->
                emissions.send(movies)
            }
        }

        assertEquals(emptyList<Movie>(), emissions.receive())

        dao.favourites.value = listOf(entity)

        assertEquals(listOf(movie), emissions.receive())

        dao.favourites.value = emptyList()

        assertEquals(emptyList<Movie>(), emissions.receive())
    }

    @Test
    fun observeIsFavourite_forwardsIdAndStatusUpdates() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(dao)
        val emissions = Channel<Boolean>(Channel.UNLIMITED)

        backgroundScope.launch {
            repository.observeIsFavourite(42).collect { status ->
                emissions.send(status)
            }
        }

        assertEquals(false, emissions.receive())
        assertEquals(42, dao.observedMovieId)

        dao.favouriteStatus.value = true

        assertEquals(true, emissions.receive())

        dao.favouriteStatus.value = false

        assertEquals(false, emissions.receive())
    }

    @Test(expected = IOException::class)
    fun save_daoFails_propagatesException() = runTest {
        val dao = FakeFavouriteMoviesDao()
        dao.error = IOException("Write failed")
        val repository = RoomFavouritesRepository(dao)

        repository.save(movie)
    }

    @Test(expected = IOException::class)
    fun getById_daoFails_propagatesException() = runTest {
        val dao = FakeFavouriteMoviesDao()
        dao.error = IOException("Read failed")
        val repository = RoomFavouritesRepository(dao)

        repository.getById(42)
    }
}

private class FakeFavouriteMoviesDao : FavouriteMoviesDao {

    val favourites =
        MutableStateFlow<List<FavouriteMovieEntity>>(emptyList())

    val favouriteStatus = MutableStateFlow(false)

    var movieResult: FavouriteMovieEntity? = null
    var error: Exception? = null

    var savedMovie: FavouriteMovieEntity? = null
        private set

    var deletedMovieId: Int? = null
        private set

    var requestedMovieId: Int? = null
        private set

    var observedMovieId: Int? = null
        private set

    override suspend fun save(movie: FavouriteMovieEntity) {
        error?.let { throw it }
        savedMovie = movie
    }

    override suspend fun deleteById(movieId: Int) {
        error?.let { throw it }
        deletedMovieId = movieId
    }

    override fun observeFavourites(): Flow<List<FavouriteMovieEntity>> {
        return favourites
    }

    override fun observeIsFavourite(movieId: Int): Flow<Boolean> {
        observedMovieId = movieId
        return favouriteStatus
    }

    override suspend fun getById(movieId: Int): FavouriteMovieEntity? {
        error?.let { throw it }
        requestedMovieId = movieId
        return movieResult
    }
}