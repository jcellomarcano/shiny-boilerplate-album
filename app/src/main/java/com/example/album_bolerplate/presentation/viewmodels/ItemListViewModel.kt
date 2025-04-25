package com.example.album_bolerplate.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.domain.usecases.GetItemPagingDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject


@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val getItemPagingDataUseCase: GetItemPagingDataUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get albumId from navigation arguments (assuming key is "albumId")
    val albumId: StateFlow<Int?> = savedStateHandle.getStateFlow("albumId", null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemPagingDataFlow: Flow<PagingData<Item>> = savedStateHandle.getStateFlow("albumId", -1) // Default -1 or handle null
        .flatMapLatest { id ->
            if (id != -1) {
                getItemPagingDataUseCase(id)
            } else {
                emptyFlow()
            }
        }
        .cachedIn(viewModelScope)
}