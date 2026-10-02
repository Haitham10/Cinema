package com.example.cinema.domain.model

data class MovieVideo(
    val id: String,
    val name: String,
    val videoKey: String,
    val site: String,
    val type: String,
    val isOfficial: Boolean
)