package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.Flow

interface IPlayerPreferencesRepository {
    suspend fun saveQueue(queue: List<Song>)
    fun getQueue(): Flow<List<Song>>
    suspend fun clearQueue()
}