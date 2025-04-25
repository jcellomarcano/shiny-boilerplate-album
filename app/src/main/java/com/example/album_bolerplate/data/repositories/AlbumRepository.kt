package com.example.album_bolerplate.data.repositories

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.album_bolerplate.data.local.dao.AlbumDao
import com.example.album_bolerplate.data.local.entities.AlbumEntity
import com.example.album_bolerplate.data.local.entities.AlbumWithItems
import com.example.album_bolerplate.data.local.entities.ItemEntity
import com.example.album_bolerplate.data.network.ApiClient
import com.example.album_bolerplate.data.network.dto.AlbumResponseDTO
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.domain.models.AlbumDetails
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

class RefreshError(message: String, cause: Throwable?) : Exception(message, cause)

@Singleton
class AlbumRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val albumDao: AlbumDao
) : IAlbumRepository {
    /**
     * Implementation of the interface method.
     * Retrieves data from the DAO (as Room entities) and maps it to Domain models.
     */
    override fun getAlbumDetailsStream(): Flow<List<AlbumDetails>> {
        return albumDao.getAllAlbumsWithItems()
            .map { roomEntitiesList ->
                roomEntitiesList.map { roomEntity ->
                    roomEntity.toAlbumDetails()
                }
            }
            .distinctUntilChanged()
    }

    /**
     * Implementation of the interface method.
     * Fetches from API, processes, stores using DAO.
     */
    override suspend fun refreshAlbums() {
        val endpoint = "https://static.leboncoin.fr/img/shared/technical-test.json"
        Log.d("AlbumRepository", "Attempting to refresh albums from $endpoint")
        try {
            val dtos: List<AlbumResponseDTO> = apiClient.fetchAlbums(endpoint)
            Log.d("AlbumRepository", "Fetched ${dtos.size} items from API.")
            processAndStoreApiResponse(dtos)
            Log.i("AlbumRepository", "Successfully processed and stored API response.")
        } catch (e: Exception) {
            Log.e("AlbumRepository", "Error during album refresh: ${e.message}", e)
            throw RefreshError("Failed to refresh albums: ${e.message}", e)
        }
    }

    override fun getAlbumPagingData(): Flow<PagingData<Album>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { albumDao.getAlbumPagingSource() }
        ).flow // Get the Flow<PagingData<Entity>>
            .map { pagingDataEntity: PagingData<AlbumEntity> ->
                pagingDataEntity.map { entity: AlbumEntity ->
                    entity.toAlbum()
                }
            }
    }

    override fun getItemPagingData(albumId: Int): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { albumDao.getItemPagingSourceForAlbum(albumId) }
        ).flow
            .map { pagingDataEntity: PagingData<ItemEntity> ->
                pagingDataEntity.map { entity: ItemEntity ->
                    entity.toItem()
                }
            }
    }

    override fun getItemById(itemId: Int): Flow<Item?> {
        return albumDao.getItemById(itemId)
            .map { entity -> entity?.toItem() }
    }

    /**
     * Private helper to process API response and store in Room DB.
     * Maps DTOs to Room Entities.
     */
    private suspend fun processAndStoreApiResponse(dtos: List<AlbumResponseDTO>) {
        if (dtos.isEmpty()) {
            Log.w("AlbumRepository", "API returned empty list. Clearing local data.")
            albumDao.clearAllData()
            return
        }
        val groupedByAlbumId = dtos.groupBy { it.albumId }

        val albumEntities = mutableListOf<AlbumEntity>()
        val itemEntities = mutableListOf<ItemEntity>()

        for ((albumId, itemsInAlbum) in groupedByAlbumId) {
            // Map all DTOs in this group to ItemEntities
            itemsInAlbum.forEach { dto -> itemEntities.add(dto.toItemEntity()) }

            val firstItemOrNull = itemsInAlbum.firstOrNull()
            val coverUrl = firstItemOrNull?.thumbnailUrl ?: ""
            val albumName = "Album $albumId"
            albumEntities.add(
                AlbumEntity(
                    id = albumId,
                    name = albumName,
                    albumCoverUrl = coverUrl
                )
            )
        }
        Log.d("AlbumRepository", "Storing ${albumEntities.size} albums and ${itemEntities.size} items.")
        albumDao.clearAndInsertAlbumsAndItems(albumEntities, itemEntities)
    }

    // --- Mapper Functions ---
    private fun AlbumWithItems.toAlbumDetails(): AlbumDetails = AlbumDetails(
        album = this.album.toAlbum(),
        items = this.items.map { it.toItem() }
    )

    private fun AlbumEntity.toAlbum(): Album = Album(
        albumId = this.id,
        name = this.name,
        albumCoverUrl = this.albumCoverUrl
    )

    private fun ItemEntity.toItem(): Item = Item(
        id = this.id,
        albumId = this.albumId,
        title = this.title,
        url = this.url,
        thumbnailUrl = this.thumbnailUrl
    )

    private fun AlbumResponseDTO.toItemEntity(): ItemEntity = ItemEntity(
        id = this.id,
        albumId = this.albumId,
        title = this.title,
        url = this.url,
        thumbnailUrl = this.thumbnailUrl
    )
}