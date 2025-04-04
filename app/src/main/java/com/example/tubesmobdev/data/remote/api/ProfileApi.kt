package com.example.tubesmobdev.data.remote.api

import com.example.tubesmobdev.data.remote.response.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>
}
