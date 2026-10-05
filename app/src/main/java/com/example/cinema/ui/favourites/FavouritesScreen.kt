package com.example.cinema.ui.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
fun FavouritesScreen(
    uiState: FavouritesUiState,
    onMovieClick: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Favourites",
            style = MaterialTheme.typography.headlineMedium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                FavouritesUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is FavouritesUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.message,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(onClick = onRetry) {
                            Text(text = "Retry")
                        }
                    }
                }

                is FavouritesUiState.Success -> {
                    if (uiState.movies.isEmpty()) {
                        Text(
                            text = "No favourites yet.\nSave a movie from its details screen.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(
                                minSize = 150.dp
                            ),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}