package com.example.cinema.data.mapper

import com.example.cinema.data.remote.dto.MovieDto
import com.example.cinema.domain.model.Movie

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
private const val POSTER_SIZE = "w500"
private const val BACKDROP_SIZE = "w780"

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        rating = voteAverage,
        overview = overview,
        posterUrl = posterPath?.let { path ->
            "$IMAGE_BASE_URL$POSTER_SIZE$path"
        },
        backdropUrl = backdropPath?.let { path ->
            "$IMAGE_BASE_URL$BACKDROP_SIZE$path"
        },
        releaseDate = releaseDate,
        genreIds = genreIds
    )
}