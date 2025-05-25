package com.example.tubesmobdev.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tubesmobdev.data.local.dao.ListeningRecordDao
import com.example.tubesmobdev.data.local.dao.SongDao
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.Song

@Database(
    entities = [Song::class, ListeningRecord::class],
    version = 6
)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun listeningRecordDao(): ListeningRecordDao
}