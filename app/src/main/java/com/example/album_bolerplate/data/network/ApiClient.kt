package com.example.album_bolerplate.data.network

import com.example.album_bolerplate.data.network.dto.AlbumResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.isSuccess
import javax.inject.Inject

class ApiClient @Inject constructor(
    private val client: HttpClient
) {

    suspend fun fetchAlbums(
        endpoint: String,
    ): List<AlbumResponseDTO> {
        val response = client.get(endpoint) {
            headers {
                append("Content-Type", "application/json")
            }
        }
        if (!response.status.isSuccess()) {
            throw Exception("Error fetching albums: ${response.status}")
        }
        val albumList: List<AlbumResponseDTO> = response.body<List<AlbumResponseDTO>>()
        return albumList
    }
}
