package com.example.cinema.data.sample

import com.example.cinema.domain.model.Movie

object SampleMovies {

    val movies: List<Movie> = listOf(
        Movie(
            id = 1,
            title = "Interstellar",
            rating = 8.5
        ),
        Movie(
            id = 2,
            title = "Inception",
            rating = 8.8
        ),
        Movie(
            id = 3,
            title = "The Dark Knight",
            rating = 9.0
        )
    )
}