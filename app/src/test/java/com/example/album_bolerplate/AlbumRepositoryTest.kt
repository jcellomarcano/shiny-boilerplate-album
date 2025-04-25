package com.example.album_bolerplate

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.turbine.test
import com.example.album_bolerplate.data.local.dao.AlbumDao
import com.example.album_bolerplate.data.local.entities.AlbumEntity
import com.example.album_bolerplate.data.local.entities.ItemEntity
import com.example.album_bolerplate.data.network.ApiClient
import com.example.album_bolerplate.data.network.dto.AlbumResponseDTO
import com.example.album_bolerplate.data.repositories.AlbumRepository
import com.example.album_bolerplate.data.repositories.RefreshError
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.domain.models.Item
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class AlbumRepositoryTest {

    // Mocks for dependencies
    @MockK
    private lateinit var mockApiClient: ApiClient

    @MockK
    private lateinit var mockAlbumDao: AlbumDao

    // Class under test
    private lateinit var repository: AlbumRepository

    // Mock PagingSources (needed because DAO methods return them)
    // We can use a simple implementation for testing purposes
    class FakeAlbumPagingSource(private val items: List<AlbumEntity> = emptyList()) : PagingSource<Int, AlbumEntity>() {
        override fun getRefreshKey(state: PagingState<Int, AlbumEntity>): Int? {
            return null
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AlbumEntity> {
            return LoadResult.Page(data = items, prevKey = null, nextKey = null)
        }
    }
    class FakeItemPagingSource(private val items: List<ItemEntity> = emptyList()) : PagingSource<Int, ItemEntity>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ItemEntity> {
            return LoadResult.Page(data = items, prevKey = null, nextKey = null)
        }
        override fun getRefreshKey(state: PagingState<Int, ItemEntity>): Int? = null
    }


    @Before
    fun setUp() {
        MockKAnnotations.init(this) // Initialize MockK annotations
        repository = AlbumRepository(mockApiClient, mockAlbumDao)

        // Default behavior for DAO PagingSource methods - return empty sources
        every { mockAlbumDao.getAlbumPagingSource() } returns FakeAlbumPagingSource()
        every { mockAlbumDao.getItemPagingSourceForAlbum(any()) } returns FakeItemPagingSource()
    }

    // --- Sample Data ---
    private val albumEntity1 = AlbumEntity(id = 1, name = "Test Album 1", albumCoverUrl = "url1")
    private val albumEntity2 = AlbumEntity(id = 2, name = "Test Album 2", albumCoverUrl = "url2")
    private val itemEntity1A = ItemEntity(id = 10, albumId = 1, title = "Item 1A", url = "url1a", thumbnailUrl = "thumb1a")
    private val itemEntity1B = ItemEntity(id = 11, albumId = 1, title = "Item 1B", url = "url1b", thumbnailUrl = "thumb1b")
    private val dto1 = AlbumResponseDTO(albumId = 1, id = 10, title = "DTO 1A", url = "url1a", thumbnailUrl = "thumb1a")
    private val dto2 = AlbumResponseDTO(albumId = 1, id = 11, title = "DTO 1B", url = "url1b", thumbnailUrl = "thumb1b")


    @Test
    fun `getItemById returns mapped item from DAO`() = runTest {
        val testItemId = 10
        val expectedItem = Item(
            id = testItemId,
            albumId = 1,
            title = "Item 1A",
            url = "url1a",
            thumbnailUrl = "thumb1a"
        )
        every { mockAlbumDao.getItemById(testItemId) } returns flowOf(itemEntity1A)

        // Act
        val resultFlow = repository.getItemById(testItemId)

        // Assert
        resultFlow.test {
            assertEquals(expectedItem, awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { mockAlbumDao.getItemById(testItemId) }
    }

    @Test
    fun `getItemById returns null when DAO returns null`() = runTest {
        // Arrange
        val testItemId = 99
        every { mockAlbumDao.getItemById(testItemId) } returns flowOf(null)

        // Act
        val resultFlow = repository.getItemById(testItemId)

        // Assert
        resultFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { mockAlbumDao.getItemById(testItemId) }
    }

    @Test
    fun `getAlbumPagingData sets up Pager with DAO source`() = runTest {
        // Arrange
        val fakeSource = FakeAlbumPagingSource(listOf(albumEntity1))
        every { mockAlbumDao.getAlbumPagingSource() } returns fakeSource

        // Act
        val flow: Flow<PagingData<Album>> = repository.getAlbumPagingData()

        // Assert
        val firstPagingData = flow.first()
        assertNotNull(firstPagingData)
        verify(atLeast = 1) { mockAlbumDao.getAlbumPagingSource() }

    }

    @Test
    fun `getItemPagingData sets up Pager with DAO source for specific album`() = runTest {
        // Arrange
        val testAlbumId = 5
        val fakeSource = FakeItemPagingSource(listOf(itemEntity1A)) // Example item
        every { mockAlbumDao.getItemPagingSourceForAlbum(testAlbumId) } returns fakeSource

        // Act
        val flow: Flow<PagingData<Item>> = repository.getItemPagingData(testAlbumId)

        // Assert
        val firstPagingData = flow.first()
        assertNotNull(firstPagingData)
        verify(atLeast = 1) { mockAlbumDao.getItemPagingSourceForAlbum(testAlbumId) }
    }

    @Test
    fun `refreshAlbums fetches from API and stores in DAO`() = runTest {
        // Arrange
        val apiResponse = listOf(dto1, dto2) // DTOs for album 1

        // Expected entities based on DTOs and repository logic
        val expectedAlbumEntity = AlbumEntity(id = 1, name = "Album 1", albumCoverUrl = "thumb1a") // Cover from first item
        val expectedItemEntity1 = ItemEntity(id = 10, albumId = 1, title = "DTO 1A", url = "url1a", thumbnailUrl = "thumb1a")
        val expectedItemEntity2 = ItemEntity(id = 11, albumId = 1, title = "DTO 1B", url = "url1b", thumbnailUrl = "thumb1b")

        coEvery { mockApiClient.fetchAlbums(any()) } returns apiResponse
        val albumSlot = slot<List<AlbumEntity>>()
        val itemSlot = slot<List<ItemEntity>>()
        coEvery { mockAlbumDao.clearAndInsertAlbumsAndItems(capture(albumSlot), capture(itemSlot)) } returns Unit

        // Act
        repository.refreshAlbums()

        // Assert
        coVerify(exactly = 1) { mockApiClient.fetchAlbums(any()) }
        coVerify(exactly = 1) { mockAlbumDao.clearAndInsertAlbumsAndItems(any(), any()) }

        // Assert captured arguments
        assertEquals(listOf(expectedAlbumEntity), albumSlot.captured)
        assertEquals(setOf(expectedItemEntity1, expectedItemEntity2), itemSlot.captured.toSet())
    }

    @Test(expected = RefreshError::class)
    fun `refreshAlbums throws RefreshError on API failure`() = runTest {
        // Arrange
        val apiException = IOException("Network failed")
        coEvery { mockApiClient.fetchAlbums(any()) } throws apiException

        // Act
        repository.refreshAlbums()

        // Assert (exception expected by @Test annotation)
    }

    @Test
    fun `refreshAlbums handles empty API response`() = runTest {
        // Arrange
        val emptyApiResponse = emptyList<AlbumResponseDTO>()
        coEvery { mockApiClient.fetchAlbums(any()) } returns emptyApiResponse
        coEvery { mockAlbumDao.clearAllData() } returns Unit

        // Act
        repository.refreshAlbums()

        // Assert
        coVerify(exactly = 1) { mockApiClient.fetchAlbums(any()) }
        coVerify(exactly = 1) { mockAlbumDao.clearAllData() }
        coVerify(exactly = 0) { mockAlbumDao.insertAlbums(any()) }
        coVerify(exactly = 0) { mockAlbumDao.insertItems(any()) }
        coVerify(exactly = 0) { mockAlbumDao.clearAndInsertAlbumsAndItems(any(), any()) }
    }
}
