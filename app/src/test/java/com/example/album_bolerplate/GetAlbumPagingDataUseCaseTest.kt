package com.example.album_bolerplate

import androidx.paging.PagingData
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import com.example.album_bolerplate.domain.usecases.GetAlbumPagingDataUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test


@ExperimentalCoroutinesApi
class GetAlbumPagingDataUseCaseTest {

    @MockK
    private lateinit var mockRepository: IAlbumRepository

    private lateinit var useCase: GetAlbumPagingDataUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this) // Initialize MockK mocks
        useCase = GetAlbumPagingDataUseCase(mockRepository)
    }

    @Test
    fun `invoke calls repository getAlbumPagingData and returns its flow`() {
        // Arrange
        val expectedFlow: Flow<PagingData<Album>> = flowOf(PagingData.empty())
        every { mockRepository.getAlbumPagingData() } returns expectedFlow

        // Act
        val resultFlow = useCase() // Invoke the use case

        // Assert
        // 1. Verify the repository method was called exactly once
        verify(exactly = 1) { mockRepository.getAlbumPagingData() }
        // 2. Verify the flow returned by the use case is the same instance
        assertEquals("The returned flow should be the one from the repository", expectedFlow, resultFlow)
    }
}
