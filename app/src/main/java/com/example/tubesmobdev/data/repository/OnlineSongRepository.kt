package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.remote.response.OnlineSong
import com.example.tubesmobdev.data.remote.api.SongApi
import javax.inject.Inject

class OnlineSongRepository @Inject constructor(private val apiService: SongApi) {
    suspend fun getTopGlobalSongs(): List<OnlineSong> = apiService.getTopGlobalSongs()
    suspend fun getTopSongsByCountry(code: String): List<OnlineSong> = apiService.getTopSongsByCountry(code)
    suspend fun getOnlineSong(id:String):OnlineSong = apiService.getOnlineSong(id)
}
