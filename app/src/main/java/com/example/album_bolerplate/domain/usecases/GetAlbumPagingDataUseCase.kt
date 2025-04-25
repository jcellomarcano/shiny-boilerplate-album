package com.example.album_bolerplate.domain.usecases

import androidx.paging.PagingData
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumPagingDataUseCase @Inject constructor(
    private val repository: IAlbumRepository
) {
    operator fun invoke(): Flow<PagingData<Album>> = repository.getAlbumPagingData()
}