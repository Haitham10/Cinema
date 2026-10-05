package com.example.cinema.data.repository

import com.example.cinema.data.local.dao.FavouriteMoviesDao
import com.example.cinema.data.local.image.FavouriteImageStore
import com.example.cinema.data.mapper.toDomain
import com.example.cinema.data.mapper.toFavouriteEntity
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.FavouritesRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomFavouritesRepository @Inject constructor(
    private val dao: FavouriteMoviesDao,
    private val imageStore: FavouriteImageStore
) : FavouritesRepository {

    private val mutationMutex = Mutex()

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
        mutationMutex.withLock {
            val existingEntity = dao.getById(movie.id)

            val entity = movie.toFavouriteEntity(
                savedAt = System.currentTimeMillis()
            ).copy(
                localPosterPath = existingEntity
                    ?.takeIf { it.posterUrl == movie.posterUrl }
                    ?.localPosterPath,

                localBackdropPath = existingEntity
                    ?.takeIf { it.backdropUrl == movie.backdropUrl }
                    ?.localBackdropPath
            )

            // Save movie data before attempting image downloads.
            dao.save(entity)

            val imagePaths = try {
                imageStore.saveImages(
                    movieId = movie.id,
                    posterUrl = movie.posterUrl,
                    backdropUrl = movie.backdropUrl
                )
            } catch (exception: IOException) {
                // Movie data is already saved.
                return@withLock
            }

            val updatedEntity = entity.copy(
                localPosterPath = imagePaths.localPosterPath
                    ?: entity.localPosterPath,

                localBackdropPath = imagePaths.localBackdropPath
                    ?: entity.localBackdropPath
            )

            if (updatedEntity != entity) {
                dao.save(updatedEntity)
            }
        }
    }

    override suspend fun deleteById(movieId: Int) {
        mutationMutex.withLock {
            // Removing the favourite is the primary operation.
            dao.deleteById(movieId)

            try {
                imageStore.deleteImages(movieId)
            } catch (exception: IOException) {
                // File cleanup failure must not undo favourite removal.
            }
        }
    }

    override suspend fun getById(movieId: Int): Movie? {
        return dao.getById(movieId)?.toDomain()
    }
}