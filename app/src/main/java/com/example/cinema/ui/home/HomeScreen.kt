package com.example.cinema.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cinema.domain.model.Movie
import com.example.cinema.ui.components.MovieCard

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "All",
        "Action",
        "Comedy",
        "Drama",
        "Horror"
    )

    var selectedCategory by rememberSaveable {
        mutableStateOf("All")
    }

    val movies = listOf(
        Movie(
            id = 1,
            title = "Interstellar",
            rating = 8.5
        ),
        Movie(
            id = 2,
            title = "Inception",
            rating = 8.8
        ),
        Movie(
            id = 3,
            title = "The Dark Knight",
            rating = 9.0
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cinema",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = categories,
                key = { category -> category }
            ) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    },
                    label = {
                        Text(text = category)
                    }
                )
            }
        }

        Text(
            text = "Discover movies",
            style = MaterialTheme.typography.titleLarge
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = movies,
                key = { movie -> movie.id }
            ) { movie ->
                MovieCard(
                    title = movie.title,
                    rating = movie.rating.toString(),
                    modifier = Modifier.width(160.dp)
                )
            }
        }
    }
}