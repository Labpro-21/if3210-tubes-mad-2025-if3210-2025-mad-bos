package com.example.tubesmobdev.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tubesmobdev.data.local.dao.SongDao
import com.example.tubesmobdev.data.model.Song

@Database(
    entities = [Song::class],
    version = 1
)
abstract class SongDatabase: RoomDatabase() {
    abstract fun songDao(): SongDao
}