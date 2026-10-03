package com.example.cinema.data.mapper

import com.example.cinema.data.local.entity.FavouriteMovieEntity
import com.example.cinema.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class FavouriteMovieMapperTest {

    private val movie = Movie(
        id = 42,
        title = "Test movie",
        rating = 8.5,
        overview = "Test overview",
        posterUrl = "https://example.com/poster.jpg",
        backdropUrl = "https://example.com/backdrop.jpg",
        releaseDate = "2026-01-15",
        genreIds = listOf(28, 12)
    )

    @Test
    fun toFavouriteEntity_preservesFieldsAndSavedAt() {
        val savedAt = 1_000L

        val entity = movie.toFavouriteEntity(savedAt)

        val expected = FavouriteMovieEntity(
            id = 42,
            title = "Test movie",
            rating = 8.5,
            overview = "Test overview",
            posterUrl = "https://example.com/poster.jpg",
            backdropUrl = "https://example.com/backdrop.jpg",
            releaseDate = "2026-01-15",
            genreIdsJson = "[28,12]",
            savedAt = savedAt
        )

        assertEquals(expected, entity)
    }

    @Test
    fun toDomain_restoresMovieFromStoredData() {
        val entity = FavouriteMovieEntity(
            id = 42,
            title = "Test movie",
            rating = 8.5,
            overview = "Test overview",
            posterUrl = "https://example.com/poster.jpg",
            backdropUrl = "https://example.com/backdrop.jpg",
            releaseDate = "2026-01-15",
            genreIdsJson = "[28, 12]",
            savedAt = 1_000L
        )

        val result = entity.toDomain()

        assertEquals(movie, result)
    }

    @Test
    fun roundTrip_preservesNullImagesAndEmptyGenres() {
        val original = movie.copy(
            posterUrl = null,
            backdropUrl = null,
            genreIds = emptyList()
        )

        val entity = original.toFavouriteEntity(savedAt = 1_000L)
        val restored = entity.toDomain()

        assertEquals("[]", entity.genreIdsJson)
        assertEquals(original, restored)
    }
}