package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.local.dao.SongDao
import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SongRepository @Inject constructor(
    private val songDao: SongDao
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    fun getLikedSongs(): Flow<List<Song>> {
       return  songDao.getLikedSongs()
    }

    suspend fun insertSong(song: Song): Result<Unit> {
        return try {
            songDao.insertSong(song)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }
    suspend fun updateLikedStatus(songId: Int, isLiked: Boolean) {
        songDao.updateLikedStatus(songId, isLiked)
    }
}