package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.local.dao.SongDao
import com.example.tubesmobdev.data.local.preferences.AuthPreferences
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.remote.api.SongApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val onlineApi: SongApi,
    private val authPreferences: IAuthPreferences
) {
    suspend fun getAllSongs(): Flow<List<Song>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            songDao.getAllSongs(userId)
        } else {
            flowOf(emptyList())
        }
    }
    suspend fun getDownloadedSongs(): Flow<List<Song>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            songDao.getDownloadedSongs(userId)
        } else {
            flowOf(emptyList())
        }
    }
    suspend fun getPlayedSongsCount(): Flow<Int> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            songDao.countPlayedSongs(userId)
        } else {
            flowOf(0)
        }
    }
    suspend fun getLikedSongs(): Flow<List<Song>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            songDao.getLikedSongs(userId)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun getNewestSongs(): Flow<List<Song>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            songDao.getNewestSongs(userId)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun getRecentlyPlayedSongs(): Flow<List<Song>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            songDao.getRecentlyPlayedSongs(userId)
        } else {
            flowOf(emptyList())
        }
    }
    suspend fun getAllSongsCount(): Flow<Int> {
        return getAllSongs().map { it.size }
    }

    suspend fun getLikedSongsCount(): Flow<Int> {
        return getLikedSongs().map { it.size }
    }
    suspend fun insertSong(song: Song): Result<Unit> {
        val userId = authPreferences.getUserId()
        userId.also { song.creatorId = it }
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

    suspend fun deleteSongDownloaded(song: Song) {
        try {
            val onlineSong = onlineApi.getOnlineSong(song.serverId.toString())
            updateSong(song.copy(isDownloaded = false, isOnline = true, filePath = onlineSong.url, coverUrl = onlineSong.artwork))
        } catch (e: HttpException) {
            songDao.deleteSong(song)
        } catch (e: IOException) {
            songDao.deleteSong(song)
        }
    }


    suspend fun updateLikedStatus(song: Song) {
        if (song.isOnline) {
            val serverId = song.serverId ?: return
            val existing = songDao.findSongByServerId(serverId)
            if (existing != null) {
                updateSong(existing.copy(isLiked = song.isLiked))
            } else {
                val newSong = song.copy(
                    isLiked = true,
                    isOnline = true,
                    isDownloaded = false
                )
                insertSong(newSong)
            }
        } else {
            songDao.updateLikedStatus(song.id, song.isLiked)
        }
    }



    suspend fun updateLastPlayed(song: Song, timestamp: Long) {
        if (song.isOnline) {
            val serverId = song.serverId ?: return
            val existing = songDao.findSongByServerId(serverId)
            if (existing != null) {
                updateSong(existing.copy(lastPlayed = timestamp))
            } else {
                val newSong = song.copy(
                    isOnline = true,
                    isDownloaded = false,
                    lastPlayed = timestamp
                )
                insertSong(newSong)
            }
        } else {
            songDao.updateLastPlayed(song.id,timestamp)

        }
    }

    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query)
    }

    suspend fun findSongByTitleAndArtist(title: String, artist: String): Song? {
        return songDao.findSongByTitleAndArtist(title, artist)
    }

    suspend fun findSongByServerId(serverId: Int): Song?{
        return songDao.findSongByServerId(serverId)
    }
    suspend fun getSongById(songId: Int, userId: Long): Song? {
        return songDao.getSongById(songId, userId)
    }

}