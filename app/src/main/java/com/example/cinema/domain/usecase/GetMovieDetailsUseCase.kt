package com.example.cinema.domain.usecase

import com.example.cinema.domain.model.Movie
import com.example.cinema.domain.repository.FavouritesRepository
import com.example.cinema.domain.repository.MoviesRepository
import java.io.IOException
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository,
    private val favouritesRepository: FavouritesRepository
) {

    suspend operator fun invoke(movieId: Int): Movie? {
        return try {
            moviesRepository.getMovieById(movieId)
        } catch (exception: IOException) {
            favouritesRepository.getById(movieId)
                ?: throw exception
        }
    }
}