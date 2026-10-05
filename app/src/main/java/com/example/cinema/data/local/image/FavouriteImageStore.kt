package com.example.cinema.data.local.image

interface FavouriteImageStore {

    suspend fun saveImages(
        movieId: Int,
        posterUrl: String?,
        backdropUrl: String?
    ): FavouriteImagePaths

    suspend fun deleteImages(movieId: Int)
}