package com.example.cinema.di

import com.example.cinema.data.repository.TmdbMoviesRepository
import com.example.cinema.domain.repository.MoviesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMoviesRepository(
        repository: TmdbMoviesRepository
    ): MoviesRepository
}