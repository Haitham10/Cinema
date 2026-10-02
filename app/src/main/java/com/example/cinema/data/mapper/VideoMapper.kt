package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.VideoDto
import com.example.cinema.domain.model.MovieVideo

fun VideoDto.toDomain(): MovieVideo {
    return MovieVideo(
        id = id,
        name = name,
        videoKey = key,
        site = site,
        type = type,
        isOfficial = official
    )
}