package com.example.album_bolerplate.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumResponseDTO(
    val id: Int,
    @SerialName("albumId")
    val albumId: Int,
    val title: String,
    val url: String,
    @SerialName("thumbnailUrl")
    val thumbnailUrl: String,
)