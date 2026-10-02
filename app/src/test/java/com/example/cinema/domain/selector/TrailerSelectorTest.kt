package com.example.cinema.domain.selector

import com.example.cinema.domain.model.MovieVideo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailerSelectorTest {

    private val selector = TrailerSelector()

    @Test
    fun select_prefersOfficialTrailer() {
        val unofficial = video(
            id = "unofficial",
            official = false
        )
        val official = video(
            id = "official",
            official = true
        )

        val result = selector.select(
            listOf(unofficial, official)
        )

        assertEquals(official, result)
    }

    @Test
    fun select_withoutOfficial_returnsFirstSuitableTrailer() {
        val first = video(id = "first")
        val second = video(id = "second")

        val result = selector.select(
            listOf(first, second)
        )

        assertEquals(first, result)
    }

    @Test
    fun select_multipleOfficialTrailers_returnsFirstOfficial() {
        val first = video(
            id = "first",
            official = true
        )
        val second = video(
            id = "second",
            official = true
        )

        val result = selector.select(
            listOf(first, second)
        )

        assertEquals(first, result)
    }

    @Test
    fun select_ignoresUnsupportedSite() {
        val unsupported = video(
            id = "unsupported",
            site = "Vimeo",
            official = true
        )
        val suitable = video(id = "suitable")

        val result = selector.select(
            listOf(unsupported, suitable)
        )

        assertEquals(suitable, result)
    }

    @Test
    fun select_ignoresNonTrailerVideos() {
        val teaser = video(
            id = "teaser",
            type = "Teaser",
            official = true
        )
        val trailer = video(id = "trailer")

        val result = selector.select(
            listOf(teaser, trailer)
        )

        assertEquals(trailer, result)
    }

    @Test
    fun select_ignoresEmptyAndWhitespaceKeys() {
        val emptyKey = video(
            id = "empty",
            key = "",
            official = true
        )
        val whitespaceKey = video(
            id = "whitespace",
            key = "   ",
            official = true
        )
        val suitable = video(id = "suitable")

        val result = selector.select(
            listOf(emptyKey, whitespaceKey, suitable)
        )

        assertEquals(suitable, result)
    }

    @Test
    fun select_acceptsDifferentLetterCase() {
        val trailer = video(
            id = "trailer",
            site = "youtube",
            type = "TRAILER"
        )

        val result = selector.select(listOf(trailer))

        assertEquals(trailer, result)
    }

    @Test
    fun select_noSuitableVideos_returnsNull() {
        val clip = video(
            id = "clip",
            type = "Clip"
        )

        val result = selector.select(listOf(clip))

        assertNull(result)
    }

    @Test
    fun select_emptyList_returnsNull() {
        val result = selector.select(emptyList())

        assertNull(result)
    }

    private fun video(
        id: String,
        site: String = "YouTube",
        type: String = "Trailer",
        key: String = "test-video-key",
        official: Boolean = false
    ): MovieVideo {
        return MovieVideo(
            id = id,
            name = "Video $id",
            videoKey = key,
            site = site,
            type = type,
            isOfficial = official
        )
    }
}