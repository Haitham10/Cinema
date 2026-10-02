package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.VideoDto
import com.example.cinema.domain.model.MovieVideo
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoMapperTest {

    @Test
    fun toDomain_mapsVideoFields() {
        val dto = VideoDto(
            id = "tmdb-video-1",
            name = "Official Trailer",
            key = "youtube-video-key",
            site = "YouTube",
            type = "Trailer",
            official = true
        )

        val result = dto.toDomain()

        assertEquals(
            MovieVideo(
                id = "tmdb-video-1",
                name = "Official Trailer",
                videoKey = "youtube-video-key",
                site = "YouTube",
                type = "Trailer",
                isOfficial = true
            ),
            result
        )
    }
}