package com.example.album_bolerplate.domain.models

data class Item(
    val albumId: Int,
    val id: Int, // Item's unique ID
    val title: String,
    val url: String,
    val thumbnailUrl: String
)