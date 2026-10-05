package com.example.cinema.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_movies")
data class FavouriteMovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val rating: Double,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val genreIdsJson: String,
    val savedAt: Long ,
    val localPosterPath: String? = null,
    val localBackdropPath: String? = null
)