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
import com.example.cinema.data.local.image.FavouriteImagePaths
import com.example.cinema.data.local.image.FavouriteImageStore
import kotlinx.coroutines.CancellationException
import org.junit.Assert.fail

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
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        val result = repository.getById(42)

        assertEquals(movie, result)
        assertEquals(42, dao.requestedMovieId)
    }

    @Test
    fun getById_missingMovie_returnsNull() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        val result = repository.getById(999)

        assertNull(result)
        assertEquals(999, dao.requestedMovieId)
    }

    @Test
    fun save_passesMovieDataAndCurrentTimeToDao() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

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
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        repository.deleteById(42)

        assertEquals(42, dao.deletedMovieId)
    }

    @Test
    fun observeFavourites_mapsUpdatesToDomainMovies() = runTest {
        val dao = FakeFavouriteMoviesDao()
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )
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
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )
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
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        repository.save(movie)
    }

    @Test(expected = IOException::class)
    fun getById_daoFails_propagatesException() = runTest {
        val dao = FakeFavouriteMoviesDao()
        dao.error = IOException("Read failed")
        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        repository.getById(42)
    }
    @Test
    fun save_persistsMovieBeforeDownloading_thenUpdatesPaths() = runTest {
        val dao = FakeFavouriteMoviesDao()

        val movieWithImages = movie.copy(
            posterUrl = "https://example.com/poster.jpg",
            backdropUrl = "https://example.com/backdrop.jpg"
        )

        val imageStore = FakeFavouriteImageStore().apply {
            result = FavouriteImagePaths(
                localPosterPath = "/test/poster.image",
                localBackdropPath = "/test/backdrop.image"
            )

            onSave = {
                val savedBeforeDownload = checkNotNull(dao.savedMovie)

                assertEquals(movieWithImages.id, savedBeforeDownload.id)
                assertEquals(movieWithImages.title, savedBeforeDownload.title)
                assertNull(savedBeforeDownload.localPosterPath)
                assertNull(savedBeforeDownload.localBackdropPath)
            }
        }

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = imageStore
        )

        repository.save(movieWithImages)

        val saved = checkNotNull(dao.savedMovie)

        assertEquals("/test/poster.image", saved.localPosterPath)
        assertEquals("/test/backdrop.image", saved.localBackdropPath)

        assertEquals(movieWithImages.id, imageStore.requestedMovieId)
        assertEquals(movieWithImages.posterUrl, imageStore.requestedPosterUrl)
        assertEquals(movieWithImages.backdropUrl, imageStore.requestedBackdropUrl)
    }

    @Test
    fun save_imageDownloadFails_movieRemainsSaved() = runTest {
        val dao = FakeFavouriteMoviesDao()

        val imageStore = FakeFavouriteImageStore().apply {
            saveError = IOException("Download failed")
        }

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = imageStore
        )

        repository.save(movie)

        val saved = checkNotNull(dao.savedMovie)

        assertEquals(movie.id, saved.id)
        assertEquals(movie.title, saved.title)
        assertNull(saved.localPosterPath)
        assertNull(saved.localBackdropPath)
    }

    @Test
    fun save_sameUrlsAndNoNewImages_preservesExistingPaths() = runTest {
        val dao = FakeFavouriteMoviesDao()

        val movieWithImages = movie.copy(
            posterUrl = "https://example.com/poster.jpg",
            backdropUrl = "https://example.com/backdrop.jpg"
        )

        dao.movieResult = entity.copy(
            posterUrl = movieWithImages.posterUrl,
            backdropUrl = movieWithImages.backdropUrl,
            localPosterPath = "/test/old-poster.image",
            localBackdropPath = "/test/old-backdrop.image"
        )

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        repository.save(movieWithImages)

        val saved = checkNotNull(dao.savedMovie)

        assertEquals("/test/old-poster.image", saved.localPosterPath)
        assertEquals("/test/old-backdrop.image", saved.localBackdropPath)
    }

    @Test
    fun save_changedUrls_doesNotReuseOldPaths() = runTest {
        val dao = FakeFavouriteMoviesDao()

        dao.movieResult = entity.copy(
            posterUrl = "https://example.com/old-poster.jpg",
            backdropUrl = "https://example.com/old-backdrop.jpg",
            localPosterPath = "/test/old-poster.image",
            localBackdropPath = "/test/old-backdrop.image"
        )

        val movieWithNewImages = movie.copy(
            posterUrl = "https://example.com/new-poster.jpg",
            backdropUrl = "https://example.com/new-backdrop.jpg"
        )

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = FakeFavouriteImageStore()
        )

        repository.save(movieWithNewImages)

        val saved = checkNotNull(dao.savedMovie)

        assertNull(saved.localPosterPath)
        assertNull(saved.localBackdropPath)
    }

    @Test
    fun deleteById_removesMovieBeforeCleaningImages() = runTest {
        val dao = FakeFavouriteMoviesDao().apply {
            movieResult = entity
        }

        val imageStore = FakeFavouriteImageStore().apply {
            onDelete = {
                assertEquals(movie.id, dao.deletedMovieId)
                assertNull(dao.movieResult)
            }
        }

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = imageStore
        )

        repository.deleteById(movie.id)

        assertEquals(movie.id, imageStore.deletedMovieId)
    }

    @Test
    fun deleteById_imageCleanupFails_movieRemainsDeleted() = runTest {
        val dao = FakeFavouriteMoviesDao().apply {
            movieResult = entity
        }

        val imageStore = FakeFavouriteImageStore().apply {
            deleteError = IOException("Cleanup failed")
        }

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = imageStore
        )

        repository.deleteById(movie.id)

        assertNull(repository.getById(movie.id))
        assertEquals(movie.id, imageStore.deletedMovieId)
    }

    @Test
    fun save_databaseWriteFails_doesNotDownloadImages() = runTest {
        val dao = FakeFavouriteMoviesDao().apply {
            saveError = IOException("Write failed")
        }

        val imageStore = FakeFavouriteImageStore()

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = imageStore
        )

        try {
            repository.save(movie)
            fail("Expected IOException")
        } catch (exception: IOException) {
            assertEquals("Write failed", exception.message)
        }

        assertNull(imageStore.requestedMovieId)
    }

    @Test
    fun save_imageCancellation_propagatesAndKeepsSavedMovie() = runTest {
        val dao = FakeFavouriteMoviesDao()

        val imageStore = FakeFavouriteImageStore().apply {
            saveError = CancellationException("Download cancelled")
        }

        val repository = RoomFavouritesRepository(
            dao = dao,
            imageStore = imageStore
        )

        try {
            repository.save(movie)
            fail("Expected CancellationException")
        } catch (exception: CancellationException) {
            assertEquals("Download cancelled", exception.message)
        }

        assertEquals(movie.id, checkNotNull(dao.savedMovie).id)
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

    var saveError: Exception? = null

    override suspend fun save(movie: FavouriteMovieEntity) {
        error?.let { throw it }
        saveError?.let { throw it }

        savedMovie = movie
        movieResult = movie
    }

    override suspend fun deleteById(movieId: Int) {
        error?.let { throw it }

        deletedMovieId = movieId

        if (movieResult?.id == movieId) {
            movieResult = null
        }
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
private class FakeFavouriteImageStore : FavouriteImageStore {

    var result = FavouriteImagePaths()

    var saveError: Exception? = null
    var deleteError: Exception? = null

    var requestedMovieId: Int? = null
        private set

    var requestedPosterUrl: String? = null
        private set

    var requestedBackdropUrl: String? = null
        private set

    var deletedMovieId: Int? = null
        private set

    var onSave: () -> Unit = {}
    var onDelete: () -> Unit = {}

    override suspend fun saveImages(
        movieId: Int,
        posterUrl: String?,
        backdropUrl: String?
    ): FavouriteImagePaths {
        requestedMovieId = movieId
        requestedPosterUrl = posterUrl
        requestedBackdropUrl = backdropUrl

        onSave()

        saveError?.let { throw it }

        return result
    }

    override suspend fun deleteImages(movieId: Int) {
        deletedMovieId = movieId

        onDelete()

        deleteError?.let { throw it }
    }
}