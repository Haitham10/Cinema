package com.example.cinema.domain.repository

import com.example.cinema.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavouritesRepository {

    fun observeFavourites(): Flow<List<Movie>>

    fun observeIsFavourite(movieId: Int): Flow<Boolean>

    suspend fun save(movie: Movie)

    suspend fun deleteById(movieId: Int)

    suspend fun getById(movieId: Int): Movie?
}