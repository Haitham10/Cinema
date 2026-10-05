package com.example.cinema.di

import android.content.Context
import androidx.room.Room
import com.example.cinema.data.local.CinemaDatabase
import com.example.cinema.data.local.dao.FavouriteMoviesDao
import com.example.cinema.data.local.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCinemaDatabase(
        @ApplicationContext context: Context
    ): CinemaDatabase {
        return Room.databaseBuilder(
            context,
            CinemaDatabase::class.java,
            "cinema_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideFavouriteMoviesDao(
        database: CinemaDatabase
    ): FavouriteMoviesDao {
        return database.favouriteMoviesDao()
    }
}