package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.MovieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `toDomain maps movie fields and builds image urls`() {
        val movieDto = MovieDto(
            id = 10,
            title = "Test Movie",
            overview = "Test overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            voteAverage = 8.5,
            releaseDate = "2026-09-27",
            genreIds = listOf(28, 12)
        )

        val movie = movieDto.toDomain()

        assertEquals(10, movie.id)
        assertEquals("Test Movie", movie.title)
        assertEquals(8.5, movie.rating, 0.0)
        assertEquals("Test overview", movie.overview)
        assertEquals(
            "https://image.tmdb.org/t/p/w500/poster.jpg",
            movie.posterUrl
        )
        assertEquals(
            "https://image.tmdb.org/t/p/w780/backdrop.jpg",
            movie.backdropUrl
        )
        assertEquals("2026-09-27", movie.releaseDate)
        assertEquals(listOf(28, 12), movie.genreIds)
    }

    @Test
    fun `toDomain keeps image urls null when paths are null`() {
        val movieDto = MovieDto(
            id = 20,
            title = "Movie Without Images",
            posterPath = null,
            backdropPath = null
        )

        val movie = movieDto.toDomain()

        assertNull(movie.posterUrl)
        assertNull(movie.backdropUrl)
    }
}