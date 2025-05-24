package com.example.tubesmobdev.data.local.preferences

import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.Flow

interface IPlayerPreferences {
    suspend fun saveQueue(queue: List<Song>)
    val getQueue: Flow<List<Song>>
    suspend fun clearQueue()
    suspend fun saveLastQueue(queue: List<Song>)
    suspend fun saveLastPlayedSong(song: Song)
    val getLastQueue: Flow<List<Song>>
    val getLastPlayedSong: Flow<Song?>
    suspend fun saveLastPosition(position: Long)
    val getLastPosition: Flow<Long>
    suspend fun clearLastSession()
}