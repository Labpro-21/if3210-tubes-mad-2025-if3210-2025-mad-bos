package com.example.tubesmobdev.data.local.preferences

import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.Flow

interface IPlayerPreferences {
    suspend fun saveQueue(queue: List<Song>)
    val getQueue: Flow<List<Song>>
    suspend fun clearQueue()
}