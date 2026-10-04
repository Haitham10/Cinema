package com.example.cinema.data.repository

import com.example.cinema.data.local.dao.FavouriteMoviesDao
import com.example.cinema.data.mapper.toDomain
import com.example.cinema.data.mapper.toFavouriteEntity
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomFavouritesRepository @Inject constructor(
    private val dao: FavouriteMoviesDao
) : FavouritesRepository {

    override fun observeFavourites(): Flow<List<Movie>> {
        return dao.observeFavourites()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
            .distinctUntilChanged()
    }

    override fun observeIsFavourite(movieId: Int): Flow<Boolean> {
        return dao.observeIsFavourite(movieId)
            .distinctUntilChanged()
    }

    override suspend fun save(movie: Movie) {
        val entity = movie.toFavouriteEntity(
            savedAt = System.currentTimeMillis()
        )

        dao.save(entity)
    }

    override suspend fun deleteById(movieId: Int) {
        dao.deleteById(movieId)
    }

    override suspend fun getById(movieId: Int): Movie? {
        return dao.getById(movieId)?.toDomain()
    }
}