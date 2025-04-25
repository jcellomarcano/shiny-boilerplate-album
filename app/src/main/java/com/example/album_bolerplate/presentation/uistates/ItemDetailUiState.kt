package com.example.album_bolerplate.presentation.uistates

import com.example.album_bolerplate.domain.models.Item

sealed interface ItemDetailUiState {
    data object Loading : ItemDetailUiState
    data class Success(val item: Item) : ItemDetailUiState
    data object NotFound : ItemDetailUiState
    data class Error(val message: String) : ItemDetailUiState
}