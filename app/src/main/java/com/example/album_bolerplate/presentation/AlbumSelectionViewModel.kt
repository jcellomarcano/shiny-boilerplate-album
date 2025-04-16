package com.example.album_bolerplate.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.album_bolerplate.data.network.ApiClient
import kotlinx.coroutines.launch

class AlbumSelectionViewModel: ViewModel() {

    val TAG = "AlmbuSelectionViewModel"
    private var _albums = mutableListOf<String>()
    private val apiClient = ApiClient()

    private val url = "https://static.leboncoin.fr/img/shared/technical-test.json"

    fun loadAlbums() {
        viewModelScope.launch {
            Log.i(TAG, "loadAlbums: ${fetchAlbums(url)}")
        }
    }

    private suspend fun fetchAlbums(endpoint: String): String {
        val response = apiClient.fetchAlbums(endpoint)
        Log.i(TAG, "fetchAlbums: $response")
        return response.toString()
    }



}