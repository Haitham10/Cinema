package com.example.cinema.data.repository

import com.example.cinema.data.mapper.toDomain
import com.example.cinema.data.remote.api.TmdbApiService
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import javax.inject.Inject

class TmdbMoviesRepository @Inject constructor(
    private val api: TmdbApiService
) : MoviesRepository {

    override suspend fun getMovies(): List<Movie> {
        return api
            .getPopularMovies()
            .results
            .map { movieDto ->
                movieDto.toDomain()
            }
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return api
            .getMovieDetails(id)
            .toDomain()
    }
}