package com.example.cinema.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrailerSection(
    state: TrailerUiState,
    onRetry: () -> Unit,
    onWatchTrailer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Trailer",
            style = MaterialTheme.typography.titleLarge
        )

        when (state) {
            TrailerUiState.Loading -> {
                CircularProgressIndicator()
            }

            is TrailerUiState.Success -> {
                Text(
                    text = state.video.name,
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = {
                        onWatchTrailer(state.video.videoKey)
                    }
                ) {
                    Text(text = "Watch trailer")
                }
            }

            TrailerUiState.Unavailable -> {
                Text(
                    text = "No trailer available.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is TrailerUiState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = onRetry
                ) {
                    Text(text = "Retry trailer")
                }
            }
        }
    }
}