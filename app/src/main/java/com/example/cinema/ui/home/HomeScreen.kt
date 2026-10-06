package com.example.cinema.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.cinema.ui.components.MovieCard
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMovieClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onGenreSelected: (Int?) -> Unit,
    onRetryGenres: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moviesSectionTitle = uiState.selectedGenreId
        ?.let { selectedGenreId ->
            uiState.genres
                .firstOrNull { genre ->
                    genre.id == selectedGenreId
                }
                ?.let { genre ->
                    "${genre.name} movies"
                }
        }
        ?: "Popular movies"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cinema",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge
        )

        when {
            uiState.isGenresLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.genresErrorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.genresErrorMessage.orEmpty(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(onClick = onRetryGenres) {
                        Text(text = "Retry categories")
                    }
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "all") {
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
                        key = { genre -> genre.id }
                    ) { genre ->
                        FilterChip(
                            selected =
                                uiState.selectedGenreId == genre.id,
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

        Text(
            text = moviesSectionTitle,
            style = MaterialTheme.typography.titleLarge
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(text = "Retry")
                    }
                }
            }

            uiState.movies.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No movies found.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.movies,
                        key = { movie -> movie.id }
                    ) { movie ->
                        MovieCard(
                            title = movie.title,
                            rating = String.format(
                                Locale.US,
                                "%.1f",
                                movie.rating
                            ),
                            posterUrl = movie.posterUrl,
                            localPosterPath = movie.localPosterPath,
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
}