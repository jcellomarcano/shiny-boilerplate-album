package com.example.album_bolerplate.di

import android.content.Context
import androidx.room.Room
import com.example.album_bolerplate.data.local.AppDatabase
import com.example.album_bolerplate.data.local.dao.AlbumDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "album_bolerplate_database"
        )
            .fallbackToDestructiveMigration(dropAllTables = true) // For testing
            .build()
    }

    @Provides
    @Singleton
    fun provideAlbumDao(database: AppDatabase): AlbumDao {
        return database.albumDao()
    }

}