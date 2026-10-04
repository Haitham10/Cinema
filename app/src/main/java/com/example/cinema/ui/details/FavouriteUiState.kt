package com.example.cinema.ui.details

data class FavouriteUiState(
    val isFavourite: Boolean? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)