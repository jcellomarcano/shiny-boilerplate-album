package com.example.album_bolerplate.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class AlbumWithItems(
    @Embedded
    val album: AlbumEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "albumId"
    )
    val items: List<ItemEntity>
)
