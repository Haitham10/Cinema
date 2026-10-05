package com.example.cinema.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavouriteSection(
    state: FavouriteUiState,
    onToggle: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonText = when {
        state.isSaving -> "Updating favourites..."
        state.isFavourite == null && state.errorMessage != null ->
            "Favourite status unavailable"
        state.isFavourite == null -> "Checking favourite status..."
        state.isFavourite == true -> "Remove from favourites"
        else -> "Add to favourites"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onToggle,
            enabled = state.isFavourite != null && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = buttonText)
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            if (state.isFavourite == null) {
                TextButton(
                    onClick = onRetry,
                    enabled = !state.isSaving
                ) {
                    Text(text = "Retry favourite status")
                }
            }
        }
    }
}