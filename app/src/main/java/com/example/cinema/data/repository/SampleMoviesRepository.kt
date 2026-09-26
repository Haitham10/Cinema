package com.example.cinema.data.repository

import com.example.cinema.data.sample.SampleMovies
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.MoviesRepository

class SampleMoviesRepository(
    private val movies: List<Movie> = SampleMovies.movies
) : MoviesRepository {

    override fun getMovies(): List<Movie> {
        return movies
    }

    override fun getMovieById(id: Int): Movie? {
        return movies.find { movie ->
            movie.id == id
        }
    }
}