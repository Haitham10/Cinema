package com.example.cinema.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.repository.FavouritesRepository
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.selector.TrailerSelector
import com.example.cinema.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsFavouriteTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5
    )

    private val viewModels = mutableListOf<MovieDetailsViewModel>()

    @After
    fun tearDown() {
        viewModels.forEach { viewModel ->
            viewModel.viewModelScope.cancel()
        }
    }

    private fun createViewModel(
        favourites: ControlledFavouritesRepository,
        movieGate: CompletableDeferred<Unit>? = null
    ): MovieDetailsViewModel {
        val viewModel = MovieDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("movieId" to movie.id)
            ),
            repository = FavouriteTestMoviesRepository(
                movie = movie,
                movieGate = movieGate
            ),
            trailerSelector = TrailerSelector(),
            favouritesRepository = favourites
        )

        viewModels.add(viewModel)
        return viewModel
    }

    private fun assertMovieVisible(viewModel: MovieDetailsViewModel) {
        val state = viewModel.uiState.value

        assertTrue(state is MovieDetailsUiState.Success)
        assertEquals(
            movie,
            (state as MovieDetailsUiState.Success).movie
        )
    }

    @Test
    fun init_unsavedMovie_statusIsFalse() = runTest {
        val favourites = ControlledFavouritesRepository()
        val viewModel = createViewModel(favourites)

        advanceUntilIdle()

        assertEquals(
            FavouriteUiState(isFavourite = false),
            viewModel.favouriteUiState.value
        )
    }

    @Test
    fun init_savedMovie_statusIsTrue() = runTest {
        val favourites = ControlledFavouritesRepository(
            initialMovies = listOf(movie)
        )
        val viewModel = createViewModel(favourites)

        advanceUntilIdle()

        assertEquals(
            FavouriteUiState(isFavourite = true),
            viewModel.favouriteUiState.value
        )
    }

    @Test
    fun toggle_unsavedMovie_savesMovie() = runTest {
        val favourites = ControlledFavouritesRepository()
        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(movie, favourites.getById(movie.id))
        assertEquals(1, favourites.saveCalls)
        assertEquals(0, favourites.deleteCalls)
        assertEquals(
            FavouriteUiState(isFavourite = true),
            viewModel.favouriteUiState.value
        )
    }

    @Test
    fun toggle_savedMovie_deletesMovie() = runTest {
        val favourites = ControlledFavouritesRepository(
            initialMovies = listOf(movie)
        )
        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertNull(favourites.getById(movie.id))
        assertEquals(0, favourites.saveCalls)
        assertEquals(1, favourites.deleteCalls)
        assertEquals(
            FavouriteUiState(isFavourite = false),
            viewModel.favouriteUiState.value
        )
    }

    @Test
    fun toggle_whileSaving_ignoresRepeatedClicks() = runTest {
        val gate = CompletableDeferred<Unit>()
        val favourites = ControlledFavouritesRepository()
        favourites.writeGate = gate

        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        viewModel.toggleFavourite()

        assertTrue(viewModel.favouriteUiState.value.isSaving)

        advanceUntilIdle()

        // The save call has started but is waiting at the gate.
        assertEquals(1, favourites.saveCalls)
        assertNull(favourites.getById(movie.id))

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(1, favourites.saveCalls)
        assertEquals(0, favourites.deleteCalls)

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(movie, favourites.getById(movie.id))
        assertEquals(
            FavouriteUiState(isFavourite = true),
            viewModel.favouriteUiState.value
        )
    }

    @Test
    fun toggle_statusUnknown_doesNotWrite() = runTest {
        val gate = CompletableDeferred<Unit>()
        val favourites = ControlledFavouritesRepository()
        favourites.readGate = gate

        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        assertMovieVisible(viewModel)
        assertNull(viewModel.favouriteUiState.value.isFavourite)

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(0, favourites.saveCalls)
        assertEquals(0, favourites.deleteCalls)

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            false,
            viewModel.favouriteUiState.value.isFavourite
        )
    }

    @Test
    fun toggle_movieStillLoading_doesNotWrite() = runTest {
        val gate = CompletableDeferred<Unit>()
        val favourites = ControlledFavouritesRepository()

        val viewModel = createViewModel(
            favourites = favourites,
            movieGate = gate
        )
        advanceUntilIdle()

        assertEquals(
            MovieDetailsUiState.Loading,
            viewModel.uiState.value
        )
        assertEquals(
            false,
            viewModel.favouriteUiState.value.isFavourite
        )

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(0, favourites.saveCalls)
        assertEquals(0, favourites.deleteCalls)

        gate.complete(Unit)
        advanceUntilIdle()

        assertMovieVisible(viewModel)
    }

    @Test
    fun saveFails_movieRemainsVisible_thenNextAttemptSucceeds() = runTest {
        val favourites = ControlledFavouritesRepository()
        favourites.writeError = IOException("Write failed")

        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(
            FavouriteUiState(
                isFavourite = false,
                errorMessage =
                    "Unable to update favourite. Please try again."
            ),
            viewModel.favouriteUiState.value
        )
        assertNull(favourites.getById(movie.id))
        assertMovieVisible(viewModel)

        favourites.writeError = null

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(2, favourites.saveCalls)
        assertEquals(movie, favourites.getById(movie.id))
        assertEquals(
            FavouriteUiState(isFavourite = true),
            viewModel.favouriteUiState.value
        )
    }

    @Test
    fun deleteFails_movieRemainsSavedAndDetailsVisible() = runTest {
        val favourites = ControlledFavouritesRepository(
            initialMovies = listOf(movie)
        )
        favourites.writeError = IOException("Delete failed")

        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(movie, favourites.getById(movie.id))
        assertEquals(
            FavouriteUiState(
                isFavourite = true,
                errorMessage =
                    "Unable to update favourite. Please try again."
            ),
            viewModel.favouriteUiState.value
        )
        assertMovieVisible(viewModel)
    }

    @Test
    fun readFails_retryRestoresStatusWithoutReloadingDetails() = runTest {
        val favourites = ControlledFavouritesRepository(
            initialMovies = listOf(movie)
        )
        favourites.readError = IOException("Read failed")

        val viewModel = createViewModel(favourites)
        advanceUntilIdle()

        assertEquals(
            FavouriteUiState(
                errorMessage =
                    "Unable to read favourite status. Please try again."
            ),
            viewModel.favouriteUiState.value
        )

        assertMovieVisible(viewModel)
        val previousDetails = viewModel.uiState.value

        favourites.readError = null

        viewModel.retryFavourite()

        assertEquals(
            FavouriteUiState(),
            viewModel.favouriteUiState.value
        )

        advanceUntilIdle()

        assertEquals(2, favourites.observationCalls)
        assertEquals(
            FavouriteUiState(isFavourite = true),
            viewModel.favouriteUiState.value
        )
        assertTrue(previousDetails === viewModel.uiState.value)
        assertFalse(viewModel.favouriteUiState.value.isSaving)
    }
}

private class FavouriteTestMoviesRepository(
    private val movie: Movie,
    private val movieGate: CompletableDeferred<Unit>? = null
) : MoviesRepository {

    override suspend fun getMovies(): List<Movie> {
        return listOf(movie)
    }

    override suspend fun getMovieById(id: Int): Movie? {
        movieGate?.await()
        return movie.takeIf { it.id == id }
    }

    override suspend fun getMovieCast(movieId: Int): List<CastMember> {
        return emptyList()
    }

    override suspend fun getMovieVideos(movieId: Int): List<MovieVideo> {
        return emptyList()
    }
}

private class ControlledFavouritesRepository(
    initialMovies: List<Movie> = emptyList()
) : FavouritesRepository {

    private val movies = MutableStateFlow(initialMovies)

    var readGate: CompletableDeferred<Unit>? = null
    var writeGate: CompletableDeferred<Unit>? = null

    var readError: Exception? = null
    var writeError: Exception? = null

    var saveCalls = 0
        private set

    var deleteCalls = 0
        private set

    var observationCalls = 0
        private set

    override fun observeFavourites(): Flow<List<Movie>> {
        return movies
    }

    override fun observeIsFavourite(movieId: Int): Flow<Boolean> {
        return flow {
            observationCalls++

            readGate?.await()
            readError?.let { throw it }

            emitAll(
                movies.map { current ->
                    current.any { it.id == movieId }
                }
            )
        }
    }

    override suspend fun save(movie: Movie) {
        saveCalls++

        writeGate?.await()
        writeError?.let { throw it }

        movies.value =
            movies.value.filterNot { it.id == movie.id } + movie
    }

    override suspend fun deleteById(movieId: Int) {
        deleteCalls++

        writeGate?.await()
        writeError?.let { throw it }

        movies.value =
            movies.value.filterNot { it.id == movieId }
    }

    override suspend fun getById(movieId: Int): Movie? {
        return movies.value.find { it.id == movieId }
    }
}