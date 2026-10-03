package com.example.cinema.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cinema.ui.components.YouTubeTrailerPlayer

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
                val video = state.video

                var showPlayer by remember(video.videoKey) {
                    mutableStateOf(false)
                }

                Text(
                    text = video.name,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (showPlayer) {
                    YouTubeTrailerPlayer(
                        videoKey = video.videoKey
                    )

                    TextButton(
                        onClick = {
                            showPlayer = false
                        }
                    ) {
                        Text(text = "Close player")
                    }
                } else {
                    Button(
                        onClick = {
                            showPlayer = true
                        }
                    ) {
                        Text(text = "Watch trailer")
                    }
                }

                TextButton(
                    onClick = {
                        showPlayer = false
                        onWatchTrailer(video.videoKey)
                    }
                ) {
                    Text(text = "Open in YouTube")
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