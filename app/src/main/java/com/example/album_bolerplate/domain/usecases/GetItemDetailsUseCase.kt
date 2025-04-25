package com.example.album_bolerplate.domain.usecases

import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemDetailsUseCase @Inject constructor(
    private val repository: IAlbumRepository
) {
    operator fun invoke(itemId: Int): Flow<Item?> = repository.getItemById(itemId)
}