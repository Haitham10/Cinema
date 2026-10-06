package com.example.cinema.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cinema.ui.components.MovieCard
import java.util.Locale

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Movie title")
            },
            placeholder = {
                Text("Search for a movie")
            },
            singleLine = true,
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    TextButton(
                        onClick = { onQueryChange("") }
                    ) {
                        Text("Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val resultState = uiState.resultState) {
                SearchResultState.Idle -> {
                    Text(
                        text = "Type a movie title to start searching.",
                        textAlign = TextAlign.Center
                    )
                }

                SearchResultState.Loading -> {
                    CircularProgressIndicator()
                }

                SearchResultState.Empty -> {
                    Text(
                        text = "No movies found. Try another title.",
                        textAlign = TextAlign.Center
                    )
                }

                is SearchResultState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = resultState.message,
                            textAlign = TextAlign.Center
                        )

                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }

                is SearchResultState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = resultState.movies,
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
                                    focusManager.clearFocus()
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