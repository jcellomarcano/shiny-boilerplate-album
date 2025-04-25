package com.example.album_bolerplate.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.album_bolerplate.domain.models.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.album_bolerplate.domain.usecases.GetAlbumPagingDataUseCase
import com.example.album_bolerplate.domain.usecases.RefreshAlbumsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumListViewModel @Inject constructor(
    getAlbumPagingDataUseCase: GetAlbumPagingDataUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase // Inject Refresh UseCase
) : ViewModel() {

    val albumPagingDataFlow: Flow<PagingData<Album>> =
        getAlbumPagingDataUseCase()
            .cachedIn(viewModelScope) // Cache paging data across config changes

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    /**
     * Called by the UI to trigger a data refresh (manual or initial if empty).
     * Handles loading state and errors.
     */
    fun refreshData() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshError.value = null
            try {
                Log.d("AlbumListViewModel", "Refreshing data via use case...")
                refreshAlbumsUseCase()
                Log.i("AlbumListViewModel", "Refresh successful.")
            } catch (e: Exception) {
                Log.e("AlbumListViewModel", "Refresh failed", e)
                _refreshError.value = e.message ?: "An unknown error occurred during refresh."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Called by the UI to clear a displayed refresh error message.
     */
    fun clearRefreshError() {
        _refreshError.value = null
    }
}