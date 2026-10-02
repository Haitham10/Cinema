package com.example.cinema.data.repository

import com.example.cinema.data.sample.SampleMovies
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.MovieVideo
class SampleMoviesRepository(
    private val movies: List<Movie> = SampleMovies.movies
) : MoviesRepository {

    override suspend fun getMovies(): List<Movie> {
        return movies
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return movies.find { movie ->
            movie.id == id
        }
    }
    override suspend fun getMovieCast(movieId: Int): List<CastMember> {
        return emptyList()
    }

    override suspend fun getMovieVideos(
        movieId: Int
    ): List<MovieVideo> {
        return emptyList()
    }
}