package com.example.album_bolerplate.domain.usecases

import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import javax.inject.Inject

class RefreshAlbumsUseCase @Inject constructor(
    private val repository: IAlbumRepository
) {
    suspend operator fun invoke() {
        repository.refreshAlbums()
    }
}