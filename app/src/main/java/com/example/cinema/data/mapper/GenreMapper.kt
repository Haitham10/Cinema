package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.GenreDto
import com.example.cinema.domain.model.Genre

fun GenreDto.toDomain(): Genre {
    return Genre(
        id = id,
        name = name
    )
}