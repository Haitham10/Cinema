package com.example.cinema.ui.home

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cinema.domain.repository.MoviesRepository

fun createHomeViewModelFactory(
    repository: MoviesRepository
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            HomeViewModel(repository = repository)
        }
    }
}