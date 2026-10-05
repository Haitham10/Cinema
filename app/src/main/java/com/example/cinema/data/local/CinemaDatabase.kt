package com.example.cinema.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cinema.data.local.dao.FavouriteMoviesDao
import com.example.cinema.data.local.entity.FavouriteMovieEntity

@Database(
    entities = [FavouriteMovieEntity::class],
    version = 2,
    exportSchema = true
)
abstract class CinemaDatabase : RoomDatabase() {

    abstract fun favouriteMoviesDao(): FavouriteMoviesDao
}