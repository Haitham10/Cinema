package com.example.cinema.ui.favourites

import androidx.lifecycle.viewModelScope
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.FavouritesRepository
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5
    )

    private val viewModels = mutableListOf<FavouritesViewModel>()

    private fun createViewModel(
        repository: ControlledFavouritesListRepository
    ): FavouritesViewModel {
        val viewModel = FavouritesViewModel(repository)
        viewModels.add(viewModel)
        return viewModel
    }

    @After
    fun tearDown() {
        viewModels.forEach { viewModel ->
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun beforeFirstResult_uiStateIsLoading() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = ControlledFavouritesListRepository()
        repository.readGate = gate

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Loading,
            viewModel.uiState.value
        )

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Success(emptyList()),
            viewModel.uiState.value
        )
    }

    @Test
    fun repositoryHasMovies_uiStateIsSuccess() = runTest {
        val repository = ControlledFavouritesListRepository(
            initialMovies = listOf(movie)
        )

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Success(listOf(movie)),
            viewModel.uiState.value
        )
    }

    @Test
    fun repositoryIsEmpty_uiStateIsEmptySuccess() = runTest {
        val repository = ControlledFavouritesListRepository()

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Success(emptyList()),
            viewModel.uiState.value
        )
    }

    @Test
    fun repositoryChanges_uiStateUpdates() = runTest {
        val repository = ControlledFavouritesListRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.save(movie)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Success(listOf(movie)),
            viewModel.uiState.value
        )

        repository.deleteById(movie.id)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Success(emptyList()),
            viewModel.uiState.value
        )

        assertEquals(1, repository.observationCalls)
    }

    @Test
    fun repositoryFails_uiStateIsError() = runTest {
        val repository = ControlledFavouritesListRepository()
        repository.readError = IOException("Read failed")

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Error(
                "Unable to load favourites. Please try again."
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun retry_afterFailure_loadsMovies() = runTest {
        val repository = ControlledFavouritesListRepository(
            initialMovies = listOf(movie)
        )
        repository.readError = IOException("Read failed")

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Error(
                "Unable to load favourites. Please try again."
            ),
            viewModel.uiState.value
        )

        repository.readError = null
        repository.readGate = CompletableDeferred()

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Loading,
            viewModel.uiState.value
        )

        repository.readGate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            FavouritesUiState.Success(listOf(movie)),
            viewModel.uiState.value
        )
        assertEquals(2, repository.observationCalls)
    }

    @Test
    fun retry_whileLoading_doesNotStartAnotherObservation() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = ControlledFavouritesListRepository()
        repository.readGate = gate

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.retry()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(1, repository.observationCalls)
        assertEquals(
            FavouritesUiState.Loading,
            viewModel.uiState.value
        )

        gate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun retry_afterSuccess_doesNotStartAnotherObservation() = runTest {
        val repository = ControlledFavouritesListRepository(
            initialMovies = listOf(movie)
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(1, repository.observationCalls)
        assertEquals(
            FavouritesUiState.Success(listOf(movie)),
            viewModel.uiState.value
        )
    }
}

private class ControlledFavouritesListRepository(
    initialMovies: List<Movie> = emptyList()
) : FavouritesRepository {

    private val movies = MutableStateFlow(initialMovies)

    var readGate: CompletableDeferred<Unit>? = null
    var readError: Exception? = null

    var observationCalls = 0
        private set

    override fun observeFavourites(): Flow<List<Movie>> {
        return flow {
            observationCalls++

            readGate?.await()
            readError?.let { throw it }

            emitAll(movies)
        }
    }

    override fun observeIsFavourite(movieId: Int): Flow<Boolean> {
        return movies.map { current ->
            current.any { it.id == movieId }
        }
    }

    override suspend fun save(movie: Movie) {
        movies.value =
            movies.value.filterNot { it.id == movie.id } + movie
    }

    override suspend fun deleteById(movieId: Int) {
        movies.value =
            movies.value.filterNot { it.id == movieId }
    }

    override suspend fun getById(movieId: Int): Movie? {
        return movies.value.find { it.id == movieId }
    }
}