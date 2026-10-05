package com.example.cinema.data.mapper

import com.example.cinema.data.local.entity.FavouriteMovieEntity
import com.example.cinema.domain.model.Movie
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Movie.toFavouriteEntity(savedAt: Long): FavouriteMovieEntity {
    return FavouriteMovieEntity(
        id = id,
        title = title,
        rating = rating,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        genreIdsJson = Json.encodeToString(genreIds),
        savedAt = savedAt,
        localPosterPath = localPosterPath,
        localBackdropPath = localBackdropPath
    )
}

fun FavouriteMovieEntity.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        rating = rating,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        genreIds = Json.decodeFromString<List<Int>>(genreIdsJson),
        localPosterPath = localPosterPath,
        localBackdropPath = localBackdropPath
    )
}