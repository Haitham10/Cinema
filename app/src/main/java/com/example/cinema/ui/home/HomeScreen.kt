package com.example.cinema.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cinema.domain.model.Movie
import com.example.cinema.ui.components.MovieCard
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMovieClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onGenreSelected: (Int?) -> Unit,
    onRetryGenres: () -> Unit,
    onRetryRandomMovies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedGenreName = uiState.genres
        .firstOrNull { genre ->
            genre.id == uiState.selectedGenreId
        }
        ?.name

    val moviesSectionTitle = if (selectedGenreName == null) {
        "Popular movies"
    } else {
        "$selectedGenreName movies"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "home_title") {
            Text(
                text = "Cinema",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        item(key = "categories_title") {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item(key = "categories_content") {
            GenresSection(
                uiState = uiState,
                onGenreSelected = onGenreSelected,
                onRetry = onRetryGenres
            )
        }

        item(key = "movies_title") {
            Text(
                text = moviesSectionTitle,
                style = MaterialTheme.typography.titleLarge
            )
        }

        item(key = "movies_content") {
            MoviesRowContent(
                movies = uiState.movies,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                emptyMessage = "No movies available.",
                onRetry = onRetry,
                onMovieClick = onMovieClick
            )
        }

        item(key = "random_movies_title") {
            Text(
                text = "Random movies",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item(key = "random_movies_content") {
            MoviesRowContent(
                movies = uiState.randomMovies,
                isLoading = uiState.isRandomMoviesLoading,
                errorMessage = uiState.randomMoviesErrorMessage,
                emptyMessage = "No random movies available.",
                onRetry = onRetryRandomMovies,
                onMovieClick = onMovieClick
            )
        }
    }
}

@Composable
private fun GenresSection(
    uiState: HomeUiState,
    onGenreSelected: (Int?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isGenresLoading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.genresErrorMessage != null -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = uiState.genresErrorMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(onClick = onRetry) {
                    Text(text = "Retry categories")
                }
            }
        }

        else -> {
            LazyRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "all_genres") {
                    FilterChip(
                        selected = uiState.selectedGenreId == null,
                        onClick = {
                            onGenreSelected(null)
                        },
                        label = {
                            Text(text = "All")
                        }
                    )
                }

                items(
                    items = uiState.genres,
                    key = { genre ->
                        genre.id
                    }
                ) { genre ->
                    FilterChip(
                        selected = (
                                uiState.selectedGenreId == genre.id
                                ),
                        onClick = {
                            onGenreSelected(genre.id)
                        },
                        label = {
                            Text(text = genre.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoviesRowContent(
    movies: List<Movie>,
    isLoading: Boolean,
    errorMessage: String?,
    emptyMessage: String,
    onRetry: () -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = onRetry
                ) {
                    Text(text = "Retry")
                }
            }
        }

        movies.isEmpty() -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            LazyRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = movies,
                    key = { movie ->
                        movie.id
                    }
                ) { movie ->
                    MovieCard(
                        title = movie.title,
                        rating = String.format(
                            Locale.US,
                            "%.1f",
                            movie.rating
                        ),
                        posterUrl = movie.posterUrl,
                        onClick = {
                            onMovieClick(movie.id)
                        },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    }
}