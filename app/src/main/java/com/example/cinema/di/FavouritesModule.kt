package com.example.cinema.di

import com.example.cinema.data.repository.RoomFavouritesRepository
import com.example.cinema.domain.repository.FavouritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavouritesModule {

    @Binds
    @Singleton
    abstract fun bindFavouritesRepository(
        repository: RoomFavouritesRepository
    ): FavouritesRepository
}