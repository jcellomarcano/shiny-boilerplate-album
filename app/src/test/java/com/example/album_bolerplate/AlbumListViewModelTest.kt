package com.example.album_bolerplate

import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.domain.usecases.GetAlbumPagingDataUseCase
import com.example.album_bolerplate.domain.usecases.RefreshAlbumsUseCase
import com.example.album_bolerplate.presentation.viewmodels.AlbumListViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException

@ExperimentalCoroutinesApi
class AlbumListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var mockGetAlbumPagingDataUseCase: GetAlbumPagingDataUseCase

    @MockK
    private lateinit var mockRefreshAlbumsUseCase: RefreshAlbumsUseCase

    private lateinit var viewModel: AlbumListViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Arrange: Define default behavior for use cases
        every { mockGetAlbumPagingDataUseCase() } returns emptyFlow<PagingData<Album>>()
        coEvery { mockRefreshAlbumsUseCase() } just Runs

        // Act: Initialize ViewModel AFTER setting up mocks
        viewModel = AlbumListViewModel(mockGetAlbumPagingDataUseCase, mockRefreshAlbumsUseCase)
    }

    @Test
    fun `init calls GetAlbumPagingDataUseCase`() {
        // Assert (ViewModel initialization happens in setUp)
        verify(exactly = 1) { mockGetAlbumPagingDataUseCase() }
        assertNotNull(viewModel.albumPagingDataFlow)
    }

    @Test
    fun `refreshData calls RefreshAlbumsUseCase and updates states correctly on success`() = runTest {
        // Arrange
        coEvery { mockRefreshAlbumsUseCase() } returns Unit

        // Act & Assert using Turbine for StateFlows
        viewModel.isRefreshing.test {
            assertFalse("Initial refreshing state should be false", awaitItem())

            viewModel.refreshData()

            assertTrue("Refreshing state should be true during refresh", awaitItem())
            assertFalse("Refreshing state should be false after refresh", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // Assert refreshError remained null
        viewModel.refreshError.test {
            assertNull("Initial error state should be null", awaitItem())
            expectNoEvents()
        }

        // Verify use case was called
        coVerify(exactly = 1) { mockRefreshAlbumsUseCase() }
    }

    @Test
    fun `refreshData calls RefreshAlbumsUseCase and updates states correctly on failure`() = runTest {
        // Arrange
        val errorMessage = "Network Error!"
        val exception = IOException(errorMessage)
        coEvery { mockRefreshAlbumsUseCase() } throws exception

        // Act & Assert isRefreshing state
        viewModel.isRefreshing.test {
            assertFalse("Initial refreshing state should be false", awaitItem())
            viewModel.refreshData()
            assertTrue("Refreshing state should be true during refresh", awaitItem())
            assertFalse("Refreshing state should be false after error", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Assert refreshError state
        viewModel.refreshError.test {
            assertEquals("Error message should be emitted", errorMessage, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Verify use case was called
        coVerify(exactly = 1) { mockRefreshAlbumsUseCase() }
    }

    @Test
    fun `refreshData prevents concurrent execution`() = runTest {
        // Arrange: Make the first call suspend indefinitely until cancelled
        val ongoingRefreshJob = Job() // Use a Job to control suspension
        coEvery { mockRefreshAlbumsUseCase() } coAnswers { ongoingRefreshJob.join() }

        // Act: Start first refresh
        viewModel.refreshData()
        advanceUntilIdle() // Let the first call start and suspend

        // Assert: Check refreshing state is true
        assertTrue("ViewModel should be refreshing", viewModel.isRefreshing.value)

        // Act: Try starting a second refresh while first is running
        viewModel.refreshData()
        advanceUntilIdle()

        // Assert: Verify use case was only called ONCE
        coVerify(exactly = 1) { mockRefreshAlbumsUseCase() }

        // Cleanup
        ongoingRefreshJob.cancel() // Allow the first refresh to complete
        advanceUntilIdle() // Let cleanup happen
        assertFalse("ViewModel should not be refreshing after cancel", viewModel.isRefreshing.value)
    }

    @Test
    fun `clearRefreshError sets error state to null`() = runTest {
        // Arrange: Simulate an error state
        val errorMessage = "Initial Error"
        coEvery { mockRefreshAlbumsUseCase() } throws IOException(errorMessage)
        viewModel.refreshData()
        advanceUntilIdle() // Ensure error state is set

        // Assert pre-condition
        assertEquals(errorMessage, viewModel.refreshError.value)

        // Act & Assert using Turbine
        viewModel.refreshError.test {
            assertEquals("Error should be present initially", errorMessage, awaitItem())
            viewModel.clearRefreshError() // Call the clear function
            assertNull("Error should become null after clear", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

// Helper Rule for swapping Main dispatcher in tests
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
