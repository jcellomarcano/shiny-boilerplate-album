package com.example.album_bolerplate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.album_bolerplate.data.local.dao.AlbumDao
import com.example.album_bolerplate.data.local.entities.AlbumEntity
import com.example.album_bolerplate.data.local.entities.ItemEntity

@Database(
    entities = [AlbumEntity::class, ItemEntity::class],
    version = 1,
    exportSchema = false,
    )
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}