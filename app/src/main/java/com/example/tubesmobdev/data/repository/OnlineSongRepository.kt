package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.local.preferences.IOnlineSongPreference
import com.example.tubesmobdev.data.remote.response.OnlineSong
import com.example.tubesmobdev.data.remote.api.SongApi
import javax.inject.Inject

class OnlineSongRepository @Inject constructor(
    private val apiService: SongApi,
    private val songPreference: IOnlineSongPreference
) {
    suspend fun getTopGlobalSongs(): List<OnlineSong> {
        return try {
            val songs = apiService.getTopGlobalSongs()
            songPreference.saveTopGlobalSongs(songs)
            songs
        } catch (e: Exception) {
            songPreference.getTopGlobalSongs() ?: emptyList()
        }
    }

    suspend fun getTopSongsByCountry(code: String): List<OnlineSong> {
        return try {
            val songs = apiService.getTopSongsByCountry(code)
            songPreference.saveTopSongsByCountry(code, songs)
            songs
        } catch (e: Exception) {
            songPreference.getTopSongsByCountry(code) ?: emptyList()
        }
    }

    suspend fun getOnlineSong(id: String): OnlineSong = apiService.getOnlineSong(id)
}