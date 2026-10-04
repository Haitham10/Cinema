package com.example.cinema.testing

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavouritesRepository : FavouritesRepository {

    private val movies =
        MutableStateFlow<List<Movie>>(emptyList())

    override fun observeFavourites(): Flow<List<Movie>> {
        return movies
    }

    override fun observeIsFavourite(movieId: Int): Flow<Boolean> {
        return movies.map { currentMovies ->
            currentMovies.any { movie ->
                movie.id == movieId
            }
        }
    }

    override suspend fun save(movie: Movie) {
        movies.value =
            movies.value.filterNot { it.id == movie.id } + movie
    }

    override suspend fun deleteById(movieId: Int) {
        movies.value =
            movies.value.filterNot { it.id == movieId }
    }

    override suspend fun getById(movieId: Int): Movie? {
        return movies.value.find { it.id == movieId }
    }
}