package com.example.cinema.domain.selector

import com.example.cinema.domain.model.MovieVideo
import javax.inject.Inject

class TrailerSelector @Inject constructor() {

    fun select(videos: List<MovieVideo>): MovieVideo? {
        val trailers = videos.filter { video ->
            video.site.equals("YouTube", ignoreCase = true) &&
                    video.type.equals("Trailer", ignoreCase = true) &&
                    video.videoKey.isNotBlank()
        }

        return trailers.firstOrNull { video ->
            video.isOfficial
        } ?: trailers.firstOrNull()
    }
}