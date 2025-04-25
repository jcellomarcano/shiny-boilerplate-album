package com.example.album_bolerplate.domain.repositories

import androidx.paging.PagingData
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.domain.models.AlbumDetails
import com.example.album_bolerplate.domain.models.Item
import kotlinx.coroutines.flow.Flow

interface IAlbumRepository {
    fun getAlbumDetailsStream(): Flow<List<AlbumDetails>>
    suspend fun refreshAlbums()

    fun getAlbumPagingData(): Flow<PagingData<Album>>

    fun getItemPagingData(albumId: Int): Flow<PagingData<Item>>

    fun getItemById(itemId: Int): Flow<Item?>

}