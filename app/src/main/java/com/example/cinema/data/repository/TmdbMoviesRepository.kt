package com.example.cinema.data.repository

import com.example.cinema.data.mapper.toDomain
import com.example.cinema.data.remote.api.TmdbApiService
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import javax.inject.Inject
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.model.Genre
import kotlin.random.Random
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
    override suspend fun searchMovies(query: String): List<Movie> {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            return emptyList()
        }

        return api
            .searchMovies(query = trimmedQuery)
            .results
            .map { movieDto ->
                movieDto.toDomain()
            }
    }
    override suspend fun getGenres(): List<Genre> {
        return api
            .getMovieGenres()
            .genres
            .map { genreDto ->
                genreDto.toDomain()
            }
    }

    override suspend fun getMoviesByGenre(
        genreId: Int
    ): List<Movie> {
        return api
            .getMoviesByGenre(genreId = genreId)
            .results
            .map { movieDto ->
                movieDto.toDomain()
            }
    }

    override suspend fun getRandomMovies(): List<Movie> {
        val randomPage = Random.nextInt(
            from = 1,
            until = 501
        )

        return api
            .getDiscoverMovies(page = randomPage)
            .results
            .map { movieDto ->
                movieDto.toDomain()
            }
    }
}