package com.example.cinema.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinema.data.local.CinemaDatabase
import com.example.cinema.data.local.entity.FavouriteMovieEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteMoviesDaoTest {

    private lateinit var database: CinemaDatabase
    private lateinit var dao: FavouriteMoviesDao

    @Before
    fun setUp() {
        val context =
            InstrumentationRegistry.getInstrumentation().targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            CinemaDatabase::class.java
        ).build()

        dao = database.favouriteMoviesDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun save_thenGetById_returnsSameMovie() = runTest {
        val movie = createMovie(id = 42)

        dao.save(movie)

        assertEquals(movie, dao.getById(42))
    }

    @Test
    fun save_existingId_updatesWithoutDuplicate() = runTest {
        val original = createMovie(id = 42)
        dao.save(original)

        val updated = original.copy(
            title = "Updated title",
            rating = 9.0,
            savedAt = 2_000L
        )

        dao.save(updated)

        assertEquals(updated, dao.getById(42))
        assertEquals(
            listOf(updated),
            dao.observeFavourites().first()
        )
    }

    @Test
    fun deleteById_removesOnlyRequestedMovie() = runTest {
        val firstMovie = createMovie(id = 1)
        val secondMovie = createMovie(id = 2)

        dao.save(firstMovie)
        dao.save(secondMovie)

        dao.deleteById(1)

        assertNull(dao.getById(1))
        assertEquals(secondMovie, dao.getById(2))
    }

    @Test
    fun observeFavourites_ordersBySavedAtThenId() = runTest {
        val older = createMovie(id = 1, savedAt = 1_000L)
        val newerHigherId = createMovie(id = 3, savedAt = 2_000L)
        val newerLowerId = createMovie(id = 2, savedAt = 2_000L)

        dao.save(newerHigherId)
        dao.save(older)
        dao.save(newerLowerId)

        assertEquals(
            listOf(newerLowerId, newerHigherId, older),
            dao.observeFavourites().first()
        )
    }

    @Test
    fun observeFavourites_emitsAfterSaveAndDelete() = runTest {
        val movie = createMovie(id = 42)
        val emissions =
            Channel<List<FavouriteMovieEntity>>(Channel.UNLIMITED)

        backgroundScope.launch {
            dao.observeFavourites()
                .distinctUntilChanged()
                .collect { movies ->
                    emissions.send(movies)
                }
        }

        assertEquals(
            emptyList<FavouriteMovieEntity>(),
            emissions.receive()
        )

        dao.save(movie)

        assertEquals(
            listOf(movie),
            emissions.receive()
        )

        dao.deleteById(movie.id)

        assertEquals(
            emptyList<FavouriteMovieEntity>(),
            emissions.receive()
        )
    }

    @Test
    fun observeIsFavourite_emitsFalseThenTrueThenFalse() = runTest {
        val movie = createMovie(id = 42)
        val emissions = Channel<Boolean>(Channel.UNLIMITED)

        backgroundScope.launch {
            dao.observeIsFavourite(movie.id)
                .distinctUntilChanged()
                .collect { isFavourite ->
                    emissions.send(isFavourite)
                }
        }

        assertFalse(emissions.receive())

        dao.save(movie)

        assertTrue(emissions.receive())

        dao.deleteById(movie.id)

        assertFalse(emissions.receive())
    }

    private fun createMovie(
        id: Int,
        savedAt: Long = 1_000L
    ): FavouriteMovieEntity {
        return FavouriteMovieEntity(
            id = id,
            title = "Movie $id",
            rating = 8.5,
            overview = "Test overview",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "2026-01-15",
            genreIdsJson = "[28,12]",
            savedAt = savedAt
        )
    }
}