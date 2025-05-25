package com.example.tubesmobdev.data.local.preferences

import com.example.tubesmobdev.data.remote.response.OnlineSong

interface IOnlineSongPreference {
    suspend fun saveTopGlobalSongs(songs: List<OnlineSong>)
    suspend fun getTopGlobalSongs(): List<OnlineSong>?

    suspend fun saveTopSongsByCountry(countryCode: String, songs: List<OnlineSong>)
    suspend fun getTopSongsByCountry(countryCode: String): List<OnlineSong>?
}