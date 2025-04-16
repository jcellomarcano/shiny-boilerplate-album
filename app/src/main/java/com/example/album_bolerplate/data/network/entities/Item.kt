package com.example.album_bolerplate.data.network.entities

data class Item(
    val albumId: Int,
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)