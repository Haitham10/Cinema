package com.example.cinema.data.repository

import com.example.cinema.data.sample.SampleMovies
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository
import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.MovieVideo
import com.example.cinema.domain.model.Genre
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

    override suspend fun searchMovies(query: String): List<Movie> {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            return emptyList()
        }

        return movies.filter { movie ->
            movie.title.contains(
                other = trimmedQuery,
                ignoreCase = true
            )
        }
    }
    override suspend fun getGenres(): List<Genre> {
        return listOf(
            Genre(id = 28, name = "Action"),
            Genre(id = 35, name = "Comedy"),
            Genre(id = 18, name = "Drama"),
            Genre(id = 27, name = "Horror")
        )
    }

    override suspend fun getMoviesByGenre(
        genreId: Int
    ): List<Movie> {
        return movies.filter { movie ->
            genreId in movie.genreIds
        }
    }
}