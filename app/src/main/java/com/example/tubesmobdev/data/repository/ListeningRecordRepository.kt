package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.local.dao.ListeningRecordDao
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.StreakEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListeningRecordRepository @Inject constructor(
    private val dao: ListeningRecordDao,
    private val authPreferences: IAuthPreferences
) {
    suspend fun insertRecord(record: ListeningRecord): Result<Unit> {
        return try {
            dao.insertRecord(record)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRecords(): Flow<List<ListeningRecord>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getAllRecords(userId)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun getTotalListeningTime(): Flow<Long> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getTotalListeningTime(userId)
                .map { it ?: 0L }
        } else {
            flowOf(0L)
        }
    }

    suspend fun getTopArtist(): Flow<String> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getTopArtist(userId)
        } else {
            flowOf("")
        }
    }

    suspend fun getTopSong(): Flow<String> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getTopSong(userId)
        } else {
            flowOf("")
        }
    }

    suspend fun getRecordsForStreakAnalysis(): List<StreakEntry> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getRecordsForStreakAnalysis(userId)
        } else {
            emptyList()
        }
    }
}