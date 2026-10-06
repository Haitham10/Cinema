package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.GenresResponseDto
import com.example.cinema.domain.model.Genre
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GenreMapperTest {

    @Test
    fun responseJson_decodesAndMapsGenres() {
        val responseJson = """
            {
                "genres": [
                    {"id": 28, "name": "Action"},
                    {"id": 35, "name": "Comedy"}
                ]
            }
        """.trimIndent()

        val response = Json.decodeFromString<GenresResponseDto>(
            responseJson
        )

        val result = response.genres.map { genreDto ->
            genreDto.toDomain()
        }

        assertEquals(
            listOf(
                Genre(id = 28, name = "Action"),
                Genre(id = 35, name = "Comedy")
            ),
            result
        )
    }

    @Test
    fun emptyResponseJson_returnsEmptyList() {
        val response = Json.decodeFromString<GenresResponseDto>(
            """{"genres": []}"""
        )

        val result = response.genres.map { it.toDomain() }

        assertEquals(emptyList<Genre>(), result)
    }
}