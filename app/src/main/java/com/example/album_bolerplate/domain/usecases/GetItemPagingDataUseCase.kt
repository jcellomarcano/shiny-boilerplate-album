package com.example.album_bolerplate.domain.usecases

import androidx.paging.PagingData
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemPagingDataUseCase @Inject constructor(
    private val repository: IAlbumRepository
) {
    operator fun invoke(albumId: Int): Flow<PagingData<Item>> =
        repository.getItemPagingData(albumId)
}