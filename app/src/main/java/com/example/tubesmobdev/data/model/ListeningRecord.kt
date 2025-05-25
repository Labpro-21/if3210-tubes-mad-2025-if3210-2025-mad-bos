package com.example.tubesmobdev.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listening_records")
data class ListeningRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Int,
    val title: String,
    val artist: String,
    val creatorId: Long?,
    val date: String,
    val durationListened: Long
)

data class DailyListeningEntry(
    val day: String,
    val minutes: Int
)