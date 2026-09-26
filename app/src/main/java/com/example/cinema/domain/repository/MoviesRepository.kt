package com.example.cinema.domain.repository

import com.example.cinema.domain.model.Movie

interface MoviesRepository {

    fun getMovies(): List<Movie>

    fun getMovieById(id: Int): Movie?
}