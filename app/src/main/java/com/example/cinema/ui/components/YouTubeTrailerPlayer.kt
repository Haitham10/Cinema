package com.example.cinema.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeTrailerPlayer(
    videoKey: String,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    key(videoKey, lifecycleOwner) {
        var isInitializing by remember {
            mutableStateOf(true)
        }

        var errorMessage by remember {
            mutableStateOf<String?>(null)
        }

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        enableAutomaticInitialization = false

                        lifecycleOwner.lifecycle.addObserver(this)

                        initialize(
                            object : AbstractYouTubePlayerListener() {

                                override fun onReady(
                                    youTubePlayer: YouTubePlayer
                                ) {
                                    isInitializing = false

                                    if (
                                        lifecycleOwner.lifecycle.currentState
                                            .isAtLeast(Lifecycle.State.RESUMED)
                                    ) {
                                        youTubePlayer.loadVideo(videoKey, 0f)
                                    } else {
                                        youTubePlayer.cueVideo(videoKey, 0f)
                                    }
                                }

                                override fun onError(
                                    youTubePlayer: YouTubePlayer,
                                    error: PlayerConstants.PlayerError
                                ) {
                                    isInitializing = false
                                    errorMessage =
                                        "Unable to play here. Try opening in YouTube."
                                }
                            }
                        )
                    }
                },
                onRelease = { playerView ->
                    lifecycleOwner.lifecycle.removeObserver(playerView)
                    playerView.release()
                },
                update = {}
            )

            if (isInitializing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}