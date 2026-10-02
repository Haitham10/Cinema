package com.example.cinema.ui.details

import com.example.cinema.domain.model.CastMember

sealed interface CastUiState {

    data object Loading : CastUiState

    data class Success(
        val members: List<CastMember>
    ) : CastUiState

    data class Error(
        val message: String
    ) : CastUiState
}