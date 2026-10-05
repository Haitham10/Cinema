package com.example.cinema.data.repository

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SampleMoviesRepositoryTest {

    private val firstMovie = Movie(
        id = 10,
        title = "First movie",
        rating = 7.5
    )

    private val secondMovie = Movie(
        id = 42,
        title = "Second movie",
        rating = 8.0
    )

    private val testMovies = listOf(firstMovie, secondMovie)

    private val repository: MoviesRepository =
        SampleMoviesRepository(movies = testMovies)

    @Test
    fun getMovies_returnsProvidedMovies() =runTest {
        val result = repository.getMovies()

        assertEquals(testMovies, result)
    }

    @Test
    fun getMovieById_existingId_returnsMatchingMovie()= runTest {
        val result = repository.getMovieById(42)

        assertEquals(secondMovie, result)
    }

    @Test
    fun getMovieById_unknownId_returnsNull()= runTest {
        val result = repository.getMovieById(999)

        assertNull(result)
    }
    @Test
    fun searchMovies_trimsQuery_andIgnoresCase() = runTest {
        val result = repository.searchMovies("  FIRST  ")

        assertEquals(listOf(firstMovie), result)
    }

    @Test
    fun searchMovies_partialTitle_returnsAllMatches() = runTest {
        val result = repository.searchMovies("mov")

        assertEquals(testMovies, result)
    }

    @Test
    fun searchMovies_blankOrUnmatchedQuery_returnsEmptyList() = runTest {
        for (query in listOf("", "   ", "Unknown title")) {
            val result = repository.searchMovies(query)

            assertEquals(emptyList<Movie>(), result)
        }
    }
}