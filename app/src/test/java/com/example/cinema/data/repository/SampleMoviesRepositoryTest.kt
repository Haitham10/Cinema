package com.example.cinema.data.repository

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
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
    fun getMovies_returnsProvidedMovies() {
        val result = repository.getMovies()

        assertEquals(testMovies, result)
    }

    @Test
    fun getMovieById_existingId_returnsMatchingMovie() {
        val result = repository.getMovieById(42)

        assertEquals(secondMovie, result)
    }

    @Test
    fun getMovieById_unknownId_returnsNull() {
        val result = repository.getMovieById(999)

        assertNull(result)
    }
}