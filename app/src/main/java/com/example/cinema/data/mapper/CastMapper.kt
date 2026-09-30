package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.CastDto
import com.example.cinema.domain.model.CastMember

private const val PROFILE_IMAGE_BASE_URL =
    "https://image.tmdb.org/t/p/w185"

fun CastDto.toDomain(): CastMember {
    return CastMember(
        id = id,
        name = name,
        character = character,
        profileUrl = profilePath
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->
                "$PROFILE_IMAGE_BASE_URL$path"
            }
    )
}