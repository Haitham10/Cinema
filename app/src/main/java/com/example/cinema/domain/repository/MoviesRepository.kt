package com.example.cinema.domain.repository

import com.example.cinema.domain.model.Movie

interface MoviesRepository {

    suspend fun getMovies(): List<Movie>

    suspend fun getMovieById(id: Int): Movie?
}