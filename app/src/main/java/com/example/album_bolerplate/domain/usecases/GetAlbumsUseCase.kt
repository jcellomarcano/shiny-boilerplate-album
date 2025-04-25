package com.example.album_bolerplate.domain.usecases

import com.example.album_bolerplate.domain.models.AlbumDetails
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val repository: IAlbumRepository
) {
    operator fun invoke(): Flow<List<AlbumDetails>> {
        return repository.getAlbumDetailsStream()
    }
}