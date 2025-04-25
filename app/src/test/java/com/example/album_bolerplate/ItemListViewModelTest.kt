package com.example.album_bolerplate

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.domain.usecases.GetItemPagingDataUseCase
import com.example.album_bolerplate.presentation.viewmodels.ItemListViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class ItemListViewModelTest {

    @get:Rule
    val mainDispatcherRule = ItemListMainDispatcherRule()

    @MockK
    private lateinit var mockGetItemPagingDataUseCase: GetItemPagingDataUseCase

    // Use a MutableMap for SavedStateHandle testing
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: ItemListViewModel

    // --- Test Data ---
    private val testAlbumId = 123
    private val invalidAlbumId = -1

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        // Initialize SavedStateHandle with no initial value for "albumId"
        savedStateHandle = SavedStateHandle()

        // Default mock behavior
        every { mockGetItemPagingDataUseCase(any()) } returns emptyFlow()
    }

    // Helper to create ViewModel after SavedStateHandle is potentially modified
    private fun createViewModel() {
        viewModel = ItemListViewModel(mockGetItemPagingDataUseCase, savedStateHandle)
    }

    @Test
    fun `albumId StateFlow reflects SavedStateHandle value`() = runTest {
        // Arrange
        savedStateHandle["albumId"] = testAlbumId
        createViewModel()
        advanceUntilIdle()

        // Assert
        viewModel.albumId.test {
            assertEquals(testAlbumId, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `albumId StateFlow reflects SavedStateHandle null value`() = runTest {
        // Arrange: No value set in SavedStateHandle initially
        createViewModel()
        advanceUntilIdle()

        // Assert
        viewModel.albumId.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `itemPagingDataFlow calls use case when albumId is valid`() = runTest {
        // Arrange
        val expectedPagingFlow: Flow<PagingData<Item>> = flowOf(PagingData.from(listOf(
            Item(albumId = testAlbumId, id = 1, title = "A", url = "", thumbnailUrl = "")
        )))
        every { mockGetItemPagingDataUseCase(testAlbumId) } returns expectedPagingFlow
        savedStateHandle["albumId"] = testAlbumId
        createViewModel()

        // Act
        val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.itemPagingDataFlow.collect {}
        }
        advanceUntilIdle()

        // Assert
        verify(exactly = 1) { mockGetItemPagingDataUseCase(testAlbumId) }

        collectionJob.cancel() // Clean up collection job
    }

    @Test
    fun `itemPagingDataFlow does NOT call use case when albumId is invalid`() = runTest {
        // Arrange
        savedStateHandle["albumId"] = invalidAlbumId
        createViewModel()

        // Act
        val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.itemPagingDataFlow.collect {}
        }
        advanceUntilIdle()

        // Assert
        verify(exactly = 0) { mockGetItemPagingDataUseCase(invalidAlbumId) }
        verify(exactly = 0) { mockGetItemPagingDataUseCase(any()) }

        collectionJob.cancel()
    }

    @Test
    fun `itemPagingDataFlow reacts to albumId changes`() = runTest {
        // Arrange: Start with invalid ID
        savedStateHandle["albumId"] = invalidAlbumId
        createViewModel()

        val validId = 456
        val flowForValidId: Flow<PagingData<Item>> = flowOf(PagingData.from(listOf(
            Item(albumId = validId, id = 2, title = "B", url = "", thumbnailUrl = "")
        )))
        every { mockGetItemPagingDataUseCase(validId) } returns flowForValidId

        // Act & Assert
        val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.itemPagingDataFlow.collect {}
        }
        advanceUntilIdle() // Initial state with invalid ID

        // Verify no call initially
        verify(exactly = 0) { mockGetItemPagingDataUseCase(any()) }

        // Change albumId in SavedStateHandle
        savedStateHandle["albumId"] = validId
        advanceUntilIdle() // Allow StateFlow/flatMapLatest to react

        // Verify use case called with the valid ID
        verify(exactly = 1) { mockGetItemPagingDataUseCase(validId) }

        // Change back to invalid ID
        savedStateHandle["albumId"] = invalidAlbumId
        advanceUntilIdle() // Allow StateFlow/flatMapLatest to react

        verify(exactly = 1) { mockGetItemPagingDataUseCase(validId) }
        verify(exactly = 0) { mockGetItemPagingDataUseCase(invalidAlbumId) }

        collectionJob.cancel() // Clean up
    }
}

// Helper Rule for swapping Main dispatcher in tests
@ExperimentalCoroutinesApi
class ItemListMainDispatcherRule(
    // Allow providing a specific scheduler for more control if needed
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

