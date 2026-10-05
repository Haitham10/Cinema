package com.example.cinema.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MovieDetailsScreen(
    uiState: MovieDetailsUiState,
    favouriteUiState: FavouriteUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onRetryCast: () -> Unit,
    onRetryTrailer: () -> Unit,
    onToggleFavourite: () -> Unit,
    onRetryFavourite: () -> Unit,
    onWatchTrailer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBackClick
        ) {
            Text(text = "Back")
        }

        when (uiState) {
            MovieDetailsUiState.Loading -> {
                CircularProgressIndicator()
            }

            is MovieDetailsUiState.Success -> {
                val movie = uiState.movie

                val backdropUrl = movie.backdropUrl
                    ?.takeIf { it.isNotBlank() }

                val imageUrl = backdropUrl
                    ?: movie.posterUrl?.takeIf { it.isNotBlank() }

                var imageMessage by remember(imageUrl) {
                    mutableStateOf<String?>(
                        if (imageUrl == null) {
                            "No image available"
                        } else {
                            "Loading image..."
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            if (backdropUrl != null) {
                                16f / 9f
                            } else {
                                2f / 3f
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = {
                                imageMessage = "Loading image..."
                            },
                            onSuccess = {
                                imageMessage = null
                            },
                            onError = {
                                imageMessage = "Image unavailable"
                            }
                        )
                    }

                    imageMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "${movie.rating} / 10",
                    style = MaterialTheme.typography.bodyLarge
                )

                FavouriteSection(
                    state = favouriteUiState,
                    onToggle = onToggleFavourite,
                    onRetry = onRetryFavourite
                )

                Text(
                    text = if (movie.releaseDate.isBlank()) {
                        "Release date unavailable"
                    } else {
                        "Release date: ${movie.releaseDate}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = movie.overview.ifBlank {
                        "No overview available."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                TrailerSection(
                    state = uiState.trailerState,
                    onRetry = onRetryTrailer,
                    onWatchTrailer = onWatchTrailer
                )

                CastSection(
                    state = uiState.castState,
                    onRetry = onRetryCast
                )
            }

            MovieDetailsUiState.NotFound -> {
                Text(
                    text = "Movie not found",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            is MovieDetailsUiState.Error -> {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = onRetry
                ) {
                    Text(text = "Retry")
                }
            }
        }
    }
}