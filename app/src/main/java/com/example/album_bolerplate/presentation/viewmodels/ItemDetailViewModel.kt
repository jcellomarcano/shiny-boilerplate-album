package com.example.album_bolerplate.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.album_bolerplate.domain.usecases.GetItemDetailsUseCase
import com.example.album_bolerplate.presentation.uistates.ItemDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val getItemDetailsUseCase: GetItemDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get itemId from navigation args (assuming key "itemId")
    private val itemId: Int = savedStateHandle.get<Int>("itemId") ?: -1

    val uiState:
            StateFlow<ItemDetailUiState> = getItemDetailsUseCase(itemId)
        .map { item ->
            if (item != null) {
                ItemDetailUiState.Success(item)
            } else {
                ItemDetailUiState.NotFound // Handle case where item ID is invalid or not found
            }
        }
        .catch { e -> emit(ItemDetailUiState.Error(e.message ?: "Failed to load item details")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ItemDetailUiState.Loading
        )
}