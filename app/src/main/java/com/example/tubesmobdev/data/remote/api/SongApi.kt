package com.example.tubesmobdev.data.remote.api

import com.example.tubesmobdev.data.remote.response.OnlineSong
import retrofit2.http.GET
import retrofit2.http.Path

interface SongApi {
    @GET("/api/top-songs/global")
    suspend fun getTopGlobalSongs(): List<OnlineSong>

    @GET("/api/top-songs/{country_code}")
    suspend fun getTopSongsByCountry(@Path("country_code") code: String): List<OnlineSong>

    @GET("/api/song/{id}")
    suspend fun getOnlineSong(@Path("id") id: String): OnlineSong
}
