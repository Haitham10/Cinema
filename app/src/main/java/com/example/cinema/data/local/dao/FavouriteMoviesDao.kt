package com.example.cinema.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.cinema.data.local.entity.FavouriteMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteMoviesDao {

    @Upsert
    suspend fun save(movie: FavouriteMovieEntity)

    @Query("DELETE FROM favourite_movies WHERE id = :movieId")
    suspend fun deleteById(movieId: Int)

    @Query(
        """
        SELECT * FROM favourite_movies
        ORDER BY savedAt DESC, id ASC
        """
    )
    fun observeFavourites(): Flow<List<FavouriteMovieEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favourite_movies WHERE id = :movieId
        )
        """
    )
    fun observeIsFavourite(movieId: Int): Flow<Boolean>

    @Query("SELECT * FROM favourite_movies WHERE id = :movieId")
    suspend fun getById(movieId: Int): FavouriteMovieEntity?
}