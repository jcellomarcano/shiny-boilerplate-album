package com.example.album_bolerplate.di

import com.example.album_bolerplate.data.repositories.AlbumRepository
import com.example.album_bolerplate.domain.repositories.IAlbumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAlbumRepository(
        albumRepository: AlbumRepository
    ): IAlbumRepository
}