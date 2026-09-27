package com.example.cinema.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MovieDetailsScreen(
    uiState: MovieDetailsUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
                Text(
                    text = uiState.movie.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "${uiState.movie.rating} / 10",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            MovieDetailsUiState.NotFound -> {
                Text(
                    text = "Movie not found",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}