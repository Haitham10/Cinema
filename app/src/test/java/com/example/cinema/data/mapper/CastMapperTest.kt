package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.CastDto
import com.example.cinema.domain.model.CastMember
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CastMapperTest {

    @Test
    fun toDomain_mapsFieldsAndBuildsProfileUrl() {
        val dto = CastDto(
            id = 42,
            name = "Test actor",
            character = "Test character",
            profilePath = "/actor.jpg"
        )

        val result = dto.toDomain()

        assertEquals(
            CastMember(
                id = 42,
                name = "Test actor",
                character = "Test character",
                profileUrl =
                    "https://image.tmdb.org/t/p/w185/actor.jpg"
            ),
            result
        )
    }

    @Test
    fun toDomain_nullProfilePath_returnsNullProfileUrl() {
        val dto = CastDto(
            id = 42,
            name = "Test actor",
            profilePath = null
        )

        assertNull(dto.toDomain().profileUrl)
    }

    @Test
    fun toDomain_blankProfilePath_returnsNullProfileUrl() {
        val dto = CastDto(
            id = 42,
            name = "Test actor",
            profilePath = "   "
        )

        assertNull(dto.toDomain().profileUrl)
    }
}