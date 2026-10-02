package com.example.cinema.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cinema.ui.components.CastMemberCard

@Composable
fun CastSection(
    state: CastUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cast",
            style = MaterialTheme.typography.titleLarge
        )

        when (state) {
            CastUiState.Loading -> {
                CircularProgressIndicator()
            }

            is CastUiState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = onRetry
                ) {
                    Text(text = "Retry cast")
                }
            }

            is CastUiState.Success -> {
                if (state.members.isEmpty()) {
                    Text(
                        text = "No cast information available.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.members
                        ) { member ->
                            CastMemberCard(
                                member = member,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}