package com.example.album_bolerplate.data.local.dao

import android.content.Context
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.album_bolerplate.data.local.AppDatabase
import com.example.album_bolerplate.data.local.entities.AlbumEntity
import com.example.album_bolerplate.data.local.entities.AlbumWithItems
import com.example.album_bolerplate.data.local.entities.ItemEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class AlbumDaoTest {

    private lateinit var albumDao: AlbumDao
    private lateinit var db: AppDatabase

    // --- Sample Data ---
    private val album1 = AlbumEntity(id = 1, name = "Test Album 1", albumCoverUrl = "url1")
    private val album2 = AlbumEntity(id = 2, name = "Test Album 2", albumCoverUrl = "url2")
    private val album3 = AlbumEntity(id = 3, name = "Test Album 3", albumCoverUrl = "url3")

    private val item1A = ItemEntity(id = 10, albumId = 1, title = "Item 1A", url = "url1a", thumbnailUrl = "thumb1a")
    private val item1B = ItemEntity(id = 11, albumId = 1, title = "Item 1B", url = "url1b", thumbnailUrl = "thumb1b")
    private val item2A = ItemEntity(id = 20, albumId = 2, title = "Item 2A", url = "url2a", thumbnailUrl = "thumb2a")
    private val itemDuplicate = ItemEntity(id = 10, albumId = 1, title = "Item 1A Updated", url = "url1a_new", thumbnailUrl = "thumb1a_new") // Same ID as item1A

    // --- Test Setup & Teardown ---

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        albumDao = db.albumDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // --- Helper to get PagingSource data ---
    private suspend fun <T : Any> PagingSource<Int, T>.getData(): List<T> {
        val pager = TestPager(PagingConfig(pageSize = 10), this)
        val result = pager.refresh() as PagingSource.LoadResult.Page
        return result.data
    }

    // --- Album Tests ---

    @Test
    fun insertAlbums_withEmptyList_doesNothing() = runTest {
        albumDao.insertAlbums(emptyList())
        val data = albumDao.getAlbumPagingSource().getData()
        assertTrue("Database should be empty after inserting empty list", data.isEmpty())
    }

    @Test
    fun insertAlbums_withSingleAlbum_insertsCorrectly() = runTest {
        albumDao.insertAlbums(listOf(album1))
        val data = albumDao.getAlbumPagingSource().getData()
        assertEquals("List should contain one album", 1, data.size)
        assertEquals("Inserted album should match", album1, data[0])
    }

    @Test
    fun insertAlbums_withMultipleAlbums_insertsAll() = runTest {
        val albums = listOf(album1, album2, album3)
        albumDao.insertAlbums(albums)
        val data = albumDao.getAlbumPagingSource().getData()
        assertEquals("List should contain 3 albums", 3, data.size)
        // Assuming default order is by ID ASC based on DAO query
        assertEquals("Albums should match inserted data in order", albums, data)
    }

    @Test
    fun insertAlbums_withDuplicateAlbums_replacesExisting() = runTest {
        albumDao.insertAlbums(listOf(album1)) // Insert initial album1
        val updatedAlbum1 = AlbumEntity(id = 1, name = "Updated Album 1", albumCoverUrl = "url1_updated")
        albumDao.insertAlbums(listOf(updatedAlbum1)) // Insert album with same ID

        val data = albumDao.getAlbumPagingSource().getData()
        assertEquals("List should contain only the updated album", 1, data.size)
        assertEquals("Album should be the updated one", updatedAlbum1, data[0])
    }

    // --- Item Tests ---

    @Test
    fun insertItems_withEmptyList_doesNothing() = runTest {
        albumDao.insertAlbums(listOf(album1)) // FK constraint
        albumDao.insertItems(emptyList())

        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData()
        assertTrue("Item list should be empty", data.isEmpty())
    }

    @Test
    fun insertItems_withSingleItem_insertsCorrectly() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))

        val retrievedItem = albumDao.getItemById(item1A.id).first()
        assertEquals("Retrieved item should match inserted item", item1A, retrievedItem)

        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData()
        assertEquals("Item list should contain one item", 1, data.size)
        assertEquals("Item in list should match inserted item", item1A, data[0])
    }

    @Test
    fun insertItems_withMultipleItems_insertsAll() = runTest {
        albumDao.insertAlbums(listOf(album1))
        val items = listOf(item1A, item1B)
        albumDao.insertItems(items)

        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData()
        assertEquals("Item list should contain 2 items", 2, data.size)
        assertEquals("Items should match inserted items in order", items, data)
    }

    @Test
    fun insertItems_withDuplicateItems_replacesExisting() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))
        albumDao.insertItems(listOf(itemDuplicate)) 

        val retrievedItem = albumDao.getItemById(item1A.id).first()
        assertEquals("Retrieved item should be the updated item", itemDuplicate, retrievedItem)

        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData()
        assertEquals("Item list should contain only the updated item", 1, data.size)
        assertEquals("Item in list should be the updated one", itemDuplicate, data[0])
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertItems_withInvalidAlbumId_throwsConstraintException() = runTest {
        val itemWithInvalidAlbum = ItemEntity(id = 99, albumId = 99, title = "Invalid", url = "url", thumbnailUrl = "thumb")
        albumDao.insertItems(listOf(itemWithInvalidAlbum))
    }

    // --- Transaction Tests ---

    @Test
    fun clearAndInsertAlbumsAndItems_withEmptyData_clearsExisting() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))
        assertNotNull("Item should exist initially", albumDao.getItemById(item1A.id).first())

        albumDao.clearAndInsertAlbumsAndItems(emptyList(), emptyList())

        assertNull("Item should be null after clear", albumDao.getItemById(item1A.id).first())
        val albumData = albumDao.getAlbumPagingSource().getData()
        assertTrue("Album list should be empty after clear", albumData.isEmpty())
    }

    @Test
    fun clearAndInsertAlbumsAndItems_withValidData_replacesData() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))

        albumDao.clearAndInsertAlbumsAndItems(listOf(album2), listOf(item2A))

        assertNull("Old item should be null after replace", albumDao.getItemById(item1A.id).first())
        // Need getAlbumById to verify album1 is gone

        assertEquals("New item should match inserted item", item2A, albumDao.getItemById(item2A.id).first())
        val albumData = albumDao.getAlbumPagingSource().getData()
        assertEquals("Album list should contain only new album", 1, albumData.size)
        assertEquals("Album should be the new one", album2, albumData[0])
    }

    @Test
    fun clearAndInsertAlbumsAndItems_withDuplicateIdsInData_insertsLastVersion() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))

        val updatedAlbum1 = AlbumEntity(id = 1, name = "Updated Album 1 Again", albumCoverUrl = "url1_updated_again")
        val updatedItem1A = ItemEntity(id = 10, albumId = 1, title = "Item 1A Updated Again", url = "url1a_new_again", thumbnailUrl = "thumb1a_new_again")
        val albumsToInsert = listOf(album2, updatedAlbum1) // album1's ID is duplicated
        val itemsToInsert = listOf(item2A, updatedItem1A) // item1A's ID is duplicated

        albumDao.clearAndInsertAlbumsAndItems(albumsToInsert, itemsToInsert)

        assertEquals("Item with duplicate ID should be updated version", updatedItem1A, albumDao.getItemById(item1A.id).first())
        assertEquals("Other new item should exist", item2A, albumDao.getItemById(item2A.id).first())

        val albumData = albumDao.getAlbumPagingSource().getData()
        assertEquals("Album list should contain 2 albums", 2, albumData.size)
        // Check content without relying on specific order if not guaranteed
        assertTrue("Album list should contain updated album 1", albumData.contains(updatedAlbum1))
        assertTrue("Album list should contain album 2", albumData.contains(album2))
    }

    @Test
    fun getAllAlbumsWithItems_initiallyEmpty() = runTest {
        val albumsWithItems = albumDao.getAllAlbumsWithItems().first() // Collect first emission
        assertTrue("Initial list should be empty", albumsWithItems.isEmpty())
    }

    @Test
    fun getAllAlbumsWithItems_withSingleAlbumAndItems() = runTest {
        // Arrange
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A, item1B))
        val expected = AlbumWithItems(album = album1, items = listOf(item1A, item1B))

        // Act
        val albumsWithItems = albumDao.getAllAlbumsWithItems().first()

        // Assert
        assertEquals("List should contain one album with items", 1, albumsWithItems.size)
        assertEquals("AlbumWithItems should match expected", expected, albumsWithItems[0])
    }

    @Test
    fun getAllAlbumsWithItems_withMultipleAlbums() = runTest {
        // Arrange
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item1A, item1B, item2A))
        val expected1 = AlbumWithItems(album = album1, items = listOf(item1A, item1B))
        val expected2 = AlbumWithItems(album = album2, items = listOf(item2A))

        // Act
        val albumsWithItems = albumDao.getAllAlbumsWithItems().first()

        // Assert
        assertEquals("List should contain two albums with items", 2, albumsWithItems.size)
        // Assuming order by album ID
        assertEquals("First AlbumWithItems should match expected1", expected1, albumsWithItems[0])
        assertEquals("Second AlbumWithItems should match expected2", expected2, albumsWithItems[1])
    }

    @Test
    fun getAllAlbumsWithItems_withAlbumAndNoItems() = runTest {
        // Arrange
        albumDao.insertAlbums(listOf(album3))
        val expected = AlbumWithItems(album = album3, items = emptyList()) // Expect empty item list

        // Act
        val albumsWithItems = albumDao.getAllAlbumsWithItems().first()

        // Assert
        assertEquals("List should contain one album", 1, albumsWithItems.size)
        assertEquals("AlbumWithItems should have empty item list", expected, albumsWithItems[0])
        assertTrue("Item list within AlbumWithItems should be empty", albumsWithItems[0].items.isEmpty())
    }

    @Test
    fun getAlbumWithItemsById_withValidId_returnsCorrectData() = runTest {
        // Arrange
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item1A, item1B, item2A))
        val expected = AlbumWithItems(album = album1, items = listOf(item1A, item1B))

        // Act
        val albumWithItems = albumDao.getAlbumWithItemsById(album1.id).first()

        // Assert
        assertNotNull("Result should not be null for valid ID", albumWithItems)
        assertEquals("Retrieved AlbumWithItems should match expected", expected, albumWithItems)
    }

    @Test
    fun getAlbumWithItemsById_withInvalidId_returnsNull() = runTest {
        // Arrange
        albumDao.insertAlbums(listOf(album1)) // Insert some data

        // Act
        val albumWithItems = albumDao.getAlbumWithItemsById(99).first() // Non-existent ID

        // Assert
        assertNull("Result should be null for invalid ID", albumWithItems)
    }

    // --- Clear Tests ---

    @Test
    fun clearAllItems_removesAllItems() = runTest {
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item1A, item1B, item2A))
        assertNotNull(albumDao.getItemById(item1A.id).first())

        albumDao.clearAllItems()

        assertNull(albumDao.getItemById(item1A.id).first())
        assertNull(albumDao.getItemById(item1B.id).first())
        assertNull(albumDao.getItemById(item2A.id).first())

        val albumData = albumDao.getAlbumPagingSource().getData()
        assertFalse("Album list should not be empty", albumData.isEmpty())
    }

    @Test
    fun clearAllItems_whenEmpty_doesNothing() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.clearAllItems() // Should not throw error
        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData()
        assertTrue("Item list should be empty", data.isEmpty())
    }

    @Test
    fun clearAllAlbums_removesAlbumsAndCascadesItems() = runTest {
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item1A, item2A))
        assertNotNull(albumDao.getItemById(item1A.id).first())

        albumDao.clearAllAlbums()

        val albumData = albumDao.getAlbumPagingSource().getData()
        assertTrue("Album list should be empty", albumData.isEmpty())

        // Verify cascade delete (assuming FK onDelete = CASCADE)
        assertNull("Item 1A should be deleted by cascade", albumDao.getItemById(item1A.id).first())
        assertNull("Item 2A should be deleted by cascade", albumDao.getItemById(item2A.id).first())
    }

    @Test
    fun clearAllAlbums_whenEmpty_doesNothing() = runTest {
        albumDao.clearAllAlbums() // Should not throw error
        val albumData = albumDao.getAlbumPagingSource().getData()
        assertTrue("Album list should be empty", albumData.isEmpty())
    }

    @Test
    fun clearAllData_removesAllAlbumsAndItems() = runTest {
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item1A, item2A))

        albumDao.clearAllData()

        val albumData = albumDao.getAlbumPagingSource().getData()
        assertTrue("Album list should be empty after clearAllData", albumData.isEmpty())

        assertNull("Item 1A should be null after clearAllData", albumDao.getItemById(item1A.id).first())
        assertNull("Item 2A should be null after clearAllData", albumDao.getItemById(item2A.id).first())
    }

    @Test
    fun clearAllData_whenEmpty_doesNothing() = runTest {
        albumDao.clearAllData() // Should not throw error

        val albumData = albumDao.getAlbumPagingSource().getData()
        assertTrue("Album list should be empty", albumData.isEmpty())

        val itemData = albumDao.getItemPagingSourceForAlbum(1).getData()
        assertTrue("Item list should be empty", itemData.isEmpty())
    }

    // --- PagingSource Tests ---

    @Test
    fun getAlbumPagingSource_withEmptyDatabase_returnsEmptyPage() = runTest {
        val data = albumDao.getAlbumPagingSource().getData()
        assertTrue("PagingSource data should be empty", data.isEmpty())
    }

    @Test
    fun getAlbumPagingSource_withMultipleAlbums_returnsCorrectPage() = runTest {
        val albums = listOf(album1, album2, album3)
        albumDao.insertAlbums(albums)
        val data = albumDao.getAlbumPagingSource().getData()
        assertEquals("PagingSource data should match inserted albums", albums, data)
    }

    @Test
    fun getAlbumPagingSource_reflectsInsertionsAfterCreation() = runTest {
        val pagingSource = albumDao.getAlbumPagingSource() // Create source first
        val data1 = pagingSource.getData()
        assertTrue("Initial PagingSource data should be empty", data1.isEmpty())

        albumDao.insertAlbums(listOf(album1, album2))

        // Re-create source to check DB state easily
        val pagingSource2 = albumDao.getAlbumPagingSource()
        val data2 = pagingSource2.getData()
        assertEquals("PagingSource data should contain inserted albums", listOf(album1, album2), data2)
    }


    @Test
    fun getItemPagingSourceForAlbum_withValidAlbum_returnsCorrectItems() = runTest {
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item1A, item1B, item2A))

        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData()
        assertEquals("PagingSource data should contain items for album 1", listOf(item1A, item1B), data)
    }

    @Test
    fun getItemPagingSourceForAlbum_withInvalidAlbum_returnsEmpty() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))

        val data = albumDao.getItemPagingSourceForAlbum(99).getData()
        assertTrue("PagingSource data should be empty for invalid album ID", data.isEmpty())
    }

    @Test
    fun getItemPagingSourceForAlbum_withNoItems_returnsEmpty() = runTest {
        albumDao.insertAlbums(listOf(album1, album2))
        albumDao.insertItems(listOf(item2A)) // Only item for album 2

        val data = albumDao.getItemPagingSourceForAlbum(album1.id).getData() // Album 1 has no items
        assertTrue("PagingSource data should be empty when album has no items", data.isEmpty())
    }

    @Test
    fun getItemPagingSourceForAlbum_reflectsInsertionsAfterCreation() = runTest {
        albumDao.insertAlbums(listOf(album1))
        val pagingSource = albumDao.getItemPagingSourceForAlbum(album1.id) // Create source
        val data1 = pagingSource.getData()
        assertTrue("Initial PagingSource data should be empty", data1.isEmpty())

        albumDao.insertItems(listOf(item1A, item1B))

        // Re-create source to check DB state easily
        val pagingSource2 = albumDao.getItemPagingSourceForAlbum(album1.id)
        val data2 = pagingSource2.getData()
        assertEquals("PagingSource data should contain inserted items", listOf(item1A, item1B), data2)
    }

    // --- Specific Item Retrieval Tests ---

    @Test
    fun getItemById_withValidId_returnsCorrectItem() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A, item1B))

        val retrievedItem = albumDao.getItemById(item1A.id).first()
        assertEquals("Retrieved item 1A should match", item1A, retrievedItem)

        val retrievedItem2 = albumDao.getItemById(item1B.id).first()
        assertEquals("Retrieved item 1B should match", item1B, retrievedItem2)
    }

    @Test
    fun getItemById_withInvalidId_returnsNull() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))

        val retrievedItem = albumDao.getItemById(999).first()
        assertNull("Retrieved item should be null for invalid ID", retrievedItem)
    }

    @Test
    fun getItemById_returnsNull_afterItemDeleted() = runTest {
        albumDao.insertAlbums(listOf(album1))
        albumDao.insertItems(listOf(item1A))
        assertNotNull("Item should exist initially", albumDao.getItemById(item1A.id).first())

        albumDao.clearAllItems() // Use clearAllItems for simplicity
        assertNull("Item should be null after deletion", albumDao.getItemById(item1A.id).first())
    }

}
