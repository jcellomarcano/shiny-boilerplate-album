package com.example.album_bolerplate.presentation.uistates

import com.example.album_bolerplate.domain.models.AlbumDetails

sealed interface AlbumUiState {
    data object Loading : AlbumUiState
    data class Success(val albums: List<AlbumDetails>) : AlbumUiState
    data class Error(val message: String) : AlbumUiState
}