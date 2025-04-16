package com.example.album_bolerplate.data.network

import com.example.album_bolerplate.data.network.dto.AlbumResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(UserAgent){
            agent = "Album-Ktor-Client / (Android App)"
        }
    }

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
