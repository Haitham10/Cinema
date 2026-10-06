package com.example.cinema.domain.repository

import com.example.cinema.domain.model.CastMember
import com.example.cinema.domain.model.Genre
import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.model.MovieVideo

interface MoviesRepository {

    suspend fun getMovies(): List<Movie>

    suspend fun getMovieById(id: Int): Movie?

    suspend fun getMovieCast(movieId: Int): List<CastMember>

    suspend fun getMovieVideos(movieId: Int): List<MovieVideo>

    suspend fun searchMovies(query: String): List<Movie>

    suspend fun getGenres(): List<Genre>

}