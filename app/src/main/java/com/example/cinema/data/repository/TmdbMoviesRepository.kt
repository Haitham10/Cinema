package com.example.cinema.data.repository

import com.example.cinema.data.mapper.toDomain
import com.example.cinema.data.remote.api.TmdbApiService
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import javax.inject.Inject
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.MovieVideo
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

    override suspend fun getMovieCast(movieId: Int): List<CastMember> {
        return api
            .getMovieCredits(movieId)
            .cast
            .map { castDto ->
                castDto.toDomain()
            }
    }

    override suspend fun getMovieVideos(
        movieId: Int
    ): List<MovieVideo> {
        return api
            .getMovieVideos(movieId)
            .results
            .map { videoDto ->
                videoDto.toDomain()
            }
    }
}