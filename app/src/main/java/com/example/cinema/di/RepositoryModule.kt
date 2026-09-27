package com.example.cinema.di

import com.example.cinema.data.repository.SampleMoviesRepository
import com.example.cinema.domain.repository.MoviesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMoviesRepository(): MoviesRepository {
        return SampleMoviesRepository()
    }
}