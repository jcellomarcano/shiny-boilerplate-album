package com.example.album_bolerplate.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.album_bolerplate.data.local.entities.AlbumEntity
import com.example.album_bolerplate.data.local.entities.AlbumWithItems
import com.example.album_bolerplate.data.local.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    /**
     * Clears existing data and inserts new albums and items within a single transaction.
     */
    @Transaction
    suspend fun clearAndInsertAlbumsAndItems(albums: List<AlbumEntity>, items: List<ItemEntity>) {
        clearAllData()
        insertAlbums(albums)
        insertItems(items)
    }

    /**
     * Gets a Flow of all albums with their associated items. Updates automatically on changes.
     */
    @Transaction // Required for queries involving @Relation
    @Query("SELECT * FROM albums ORDER BY id ASC")
    fun getAllAlbumsWithItems(): Flow<List<AlbumWithItems>>

    /**
     * Gets a Flow of a single album with its items by ID. Updates automatically.
     */
    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getAlbumWithItemsById(albumId: Int): Flow<AlbumWithItems?> // Nullable if album might not exist

    // --- Delete Operations ---

    @Query("DELETE FROM items")
    suspend fun clearAllItems()

    @Query("DELETE FROM albums")
    suspend fun clearAllAlbums()

    // --- Paging Methods ---

    @Query("SELECT * FROM albums ORDER BY id ASC")
    fun getAlbumPagingSource(): PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM items WHERE albumId = :albumId ORDER BY id ASC")
    fun getItemPagingSourceForAlbum(albumId: Int): PagingSource<Int, ItemEntity>

    // --- Detail Method (for Screen 3) ---
    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getItemById(itemId: Int): Flow<ItemEntity?>

    @Transaction
    suspend fun clearAllData() {
        clearAllItems()
        clearAllAlbums()
    }

}