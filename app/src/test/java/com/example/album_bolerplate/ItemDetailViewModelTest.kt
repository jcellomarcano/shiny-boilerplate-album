package com.example.album_bolerplate

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.domain.usecases.GetItemDetailsUseCase
import com.example.album_bolerplate.presentation.uistates.ItemDetailUiState
import com.example.album_bolerplate.presentation.viewmodels.ItemDetailViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException

@ExperimentalCoroutinesApi
class ItemDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = ItemDetailMainDispatcherRule()

    @MockK
    private lateinit var mockGetItemDetailsUseCase: GetItemDetailsUseCase

    // Use a real SavedStateHandle, pre-populated for tests
    private lateinit var savedStateHandle: SavedStateHandle

    // The ViewModel under test
    private lateinit var viewModel: ItemDetailViewModel

    // --- Test Constants ---
    private val testItemId = 42
    private val invalidItemId = -1
    private val testItem = Item(id = testItemId, albumId = 1, title = "Test Item Detail", url = "url", thumbnailUrl = "thumb")
    private val itemIdKey = "itemId" // Key used in SavedStateHandle and Navigation

    @Before
    fun setUp() {
        MockKAnnotations.init(this) // Initialize MockK annotations
        every { mockGetItemDetailsUseCase(any()) } returns flowOf(null)
    }

    // Helper function to create ViewModel with a specific itemId in SavedStateHandle
    private fun createViewModelWithItemId(itemId: Int) {
        savedStateHandle = SavedStateHandle(mapOf(itemIdKey to itemId))
        viewModel = ItemDetailViewModel(mockGetItemDetailsUseCase, savedStateHandle)
    }

    // Helper function to create ViewModel with no itemId in SavedStateHandle
    private fun createViewModelWithNoItemId() {
        savedStateHandle = SavedStateHandle()
        viewModel = ItemDetailViewModel(mockGetItemDetailsUseCase, savedStateHandle)
    }


    @Test
    fun `uiState starts with Loading then emits NotFound when itemId is invalid`() = runTest {
        createViewModelWithItemId(invalidItemId)

        // Assert
        viewModel.uiState.test {
            assertEquals("Initial state should be Loading", ItemDetailUiState.Loading, awaitItem())
            assertEquals("Next state should be NotFound for invalid ID", ItemDetailUiState.NotFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // Verify use case was called with the invalid ID
        verify { mockGetItemDetailsUseCase(invalidItemId) }
    }

    @Test
    fun `uiState starts with Loading then emits NotFound when itemId key is missing`() = runTest {
        createViewModelWithNoItemId()

        // Assert
        viewModel.uiState.test {
            assertEquals("Initial state should be Loading", ItemDetailUiState.Loading, awaitItem())
            assertEquals("Next state should be NotFound when key is missing", ItemDetailUiState.NotFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verify { mockGetItemDetailsUseCase(invalidItemId) }
    }


    @Test
    fun `uiState emits Success when use case returns item for valid itemId`() = runTest {
        every { mockGetItemDetailsUseCase(testItemId) } returns flowOf(testItem)
        createViewModelWithItemId(testItemId)

        // Assert
        viewModel.uiState.test {
            assertEquals("Initial state should be Loading", ItemDetailUiState.Loading, awaitItem())
            assertEquals("Next state should be Success", ItemDetailUiState.Success(testItem), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // Verify use case was called with the correct ID
        verify { mockGetItemDetailsUseCase(testItemId) }
    }

    @Test
    fun `uiState emits NotFound when use case returns null for valid itemId`() = runTest {
        createViewModelWithItemId(testItemId)

        // Assert
        viewModel.uiState.test {
            assertEquals("Initial state should be Loading", ItemDetailUiState.Loading, awaitItem())
            assertEquals("Next state should be NotFound", ItemDetailUiState.NotFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // Verify use case was called with the correct ID
        verify { mockGetItemDetailsUseCase(testItemId) }
    }

    @Test
    fun `uiState emits Error when use case throws exception`() = runTest {
        // Arrange
        val errorMessage = "Database error!"
        val exception = IOException(errorMessage)
        // Mock use case to throw an error
        every { mockGetItemDetailsUseCase(testItemId) } returns flow { throw exception }
        // Create ViewModel with the valid test ID
        createViewModelWithItemId(testItemId)

        // Assert
        viewModel.uiState.test {
            assertEquals("Initial state should be Loading", ItemDetailUiState.Loading, awaitItem())
            // Check the emitted error state
            val errorState = awaitItem()
            assertTrue("Next state should be Error", errorState is ItemDetailUiState.Error)
            assertEquals("Error message should match", errorMessage, (errorState as ItemDetailUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
        // Verify use case was called with the correct ID
        verify { mockGetItemDetailsUseCase(testItemId) }
    }
}

@ExperimentalCoroutinesApi
class ItemDetailMainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
