package com.example.cinema.ui.search

import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movie = Movie(
        id = 42,
        title = "Batman",
        rating = 8.5
    )

    @Test
    fun init_isIdleWithoutRequests() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)

        runCurrent()

        assertEquals(SearchUiState(), viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.requests)
    }

    @Test
    fun queryChange_waits400ms_thenReturnsSuccess() = runTest {
        val repository = FakeSearchRepository().apply {
            respond = { listOf(movie) }
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("  Batman  ")
        runCurrent()

        assertEquals(
            SearchUiState(
                query = "  Batman  ",
                resultState = SearchResultState.Loading
            ),
            viewModel.uiState.value
        )

        advanceTimeBy(399)
        runCurrent()

        assertEquals(emptyList<String>(), repository.requests)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("Batman"), repository.requests)
        assertEquals(
            SearchUiState(
                query = "  Batman  ",
                resultState = SearchResultState.Success(listOf(movie))
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun typingAgain_restartsDebounce() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("Bat")
        runCurrent()

        advanceTimeBy(300)

        viewModel.onQueryChange("Batman")
        runCurrent()

        advanceTimeBy(399)
        runCurrent()

        assertEquals(emptyList<String>(), repository.requests)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("Batman"), repository.requests)
    }

    @Test
    fun blankQuery_cancelsPendingSearch_andReturnsIdle() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("Batman")
        runCurrent()

        advanceTimeBy(200)

        viewModel.onQueryChange("   ")
        advanceUntilIdle()

        assertEquals(emptyList<String>(), repository.requests)
        assertEquals(
            SearchUiState(
                query = "   ",
                resultState = SearchResultState.Idle
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun clearQuery_cancelsRunningSearch() = runTest {
        val gate = CompletableDeferred<List<Movie>>()

        val repository = FakeSearchRepository().apply {
            respond = { gate.await() }
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("Batman")
        runCurrent()

        advanceTimeBy(400)
        runCurrent()

        assertEquals(listOf("Batman"), repository.requests)

        viewModel.onQueryChange("")
        runCurrent()

        assertEquals(
            listOf("Batman"),
            repository.cancelledQueries
        )
        assertEquals(SearchUiState(), viewModel.uiState.value)
    }

    @Test
    fun searchWithoutResults_returnsEmpty() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("Unknown movie")
        advanceUntilIdle()

        assertEquals(
            SearchUiState(
                query = "Unknown movie",
                resultState = SearchResultState.Empty
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun retry_afterFailure_searchesWithoutDebounce() = runTest {
        val repository = FakeSearchRepository().apply {
            respond = {
                throw IOException("Connection failed")
            }
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("Batman")
        advanceUntilIdle()

        assertEquals(
            SearchResultState.Error(
                "Unable to search movies. Please try again."
            ),
            viewModel.uiState.value.resultState
        )

        repository.respond = { listOf(movie) }

        viewModel.retry()

        assertEquals(
            SearchResultState.Loading,
            viewModel.uiState.value.resultState
        )

        // Run queued work without advancing virtual time.
        runCurrent()

        assertEquals(
            listOf("Batman", "Batman"),
            repository.requests
        )
        assertEquals(
            SearchResultState.Success(listOf(movie)),
            viewModel.uiState.value.resultState
        )

        // Retry outside Error should do nothing.
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, repository.requests.size)
    }

    @Test
    fun olderSearchFinishesLate_doesNotReplaceLatestResults() = runTest {
        val oldGate = CompletableDeferred<List<Movie>>()

        val oldMovie = movie.copy(
            id = 1,
            title = "Old result"
        )
        val latestMovie = movie.copy(
            id = 2,
            title = "Latest result"
        )

        val repository = FakeSearchRepository().apply {
            respond = { query ->
                if (query == "Old") {
                    // Simulate work that delays responding to cancellation.
                    withContext(NonCancellable) {
                        oldGate.await()
                    }
                } else {
                    listOf(latestMovie)
                }
            }
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("Old")
        runCurrent()

        advanceTimeBy(400)
        runCurrent()

        viewModel.onQueryChange("Latest")
        runCurrent()

        advanceTimeBy(400)
        runCurrent()

        val expectedState = SearchUiState(
            query = "Latest",
            resultState = SearchResultState.Success(
                listOf(latestMovie)
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        oldGate.complete(listOf(oldMovie))
        advanceUntilIdle()

        assertEquals(listOf("Old", "Latest"), repository.requests)
        assertEquals(expectedState, viewModel.uiState.value)
    }
}

private class FakeSearchRepository : MoviesRepository {

    val requests = mutableListOf<String>()
    val cancelledQueries = mutableListOf<String>()

    var respond: suspend (String) -> List<Movie> = {
        emptyList()
    }

    override suspend fun searchMovies(query: String): List<Movie> {
        requests.add(query)

        return try {
            respond(query)
        } catch (exception: CancellationException) {
            cancelledQueries.add(query)
            throw exception
        }
    }

    override suspend fun getMovies(): List<Movie> {
        error("getMovies is not used in Search tests")
    }

    override suspend fun getMovieById(id: Int): Movie? {
        error("getMovieById is not used in Search tests")
    }

    override suspend fun getMovieCast(movieId: Int): List<CastMember> {
        error("getMovieCast is not used in Search tests")
    }

    override suspend fun getMovieVideos(movieId: Int): List<MovieVideo> {
        error("getMovieVideos is not used in Search tests")
    }

    override suspend fun getGenres(): List<Genre> {
        error("getGenres is not configured for this test")
    }

    override suspend fun getMoviesByGenre(
        genreId: Int
    ): List<Movie> {
        error("getMoviesByGenre is not configured for this test")
    }

    override suspend fun getRandomMovies(): List<Movie> {
        error("getRandomMovies is not configured for this test")
    }
}