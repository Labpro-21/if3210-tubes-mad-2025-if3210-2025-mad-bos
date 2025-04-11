package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.local.preferences.IPlayerPreferences
import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPreferencesRepository @Inject constructor(
    private val playerPreferences: IPlayerPreferences
) : IPlayerPreferencesRepository {

    override suspend fun saveQueue(queue: List<Song>) {
        playerPreferences.saveQueue(queue)
    }

    override fun getQueue(): Flow<List<Song>> {
        return playerPreferences.getQueue
    }

    override suspend fun clearQueue() {
        playerPreferences.clearQueue()
    }
}