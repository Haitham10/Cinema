package com.example.cinema.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File
@Composable
fun MovieCard(
    title: String,
    rating: String,
    onClick: () -> Unit,
    posterUrl: String? = null,
    modifier: Modifier = Modifier,
    localPosterPath: String? = null
) {
    val remoteUrl = posterUrl?.takeIf { it.isNotBlank() }
    val localPath = localPosterPath?.takeIf { it.isNotBlank() }

    var localImageFailed by remember(localPath, remoteUrl) {
        mutableStateOf(false)
    }

    val imageModel: Any? = remember(
        localPath,
        remoteUrl,
        localImageFailed
    ) {
        if (localPath != null && !localImageFailed) {
            File(localPath)
        } else {
            remoteUrl
        }
    }

    var posterMessage by remember(imageModel) {
        mutableStateOf<String?>(
            if (imageModel == null) {
                "No poster available"
            } else {
                "Loading poster..."
            }
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            posterMessage = "Loading poster..."
                        },
                        onSuccess = {
                            posterMessage = null
                        },
                        onError = {
                            if (imageModel is File && remoteUrl != null) {
                                localImageFailed = true
                            } else {
                                posterMessage = "Poster unavailable"
                            }
                        }
                    )
                }

                posterMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$rating / 10",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}