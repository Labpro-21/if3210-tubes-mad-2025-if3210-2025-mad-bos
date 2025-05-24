package com.example.tubesmobdev.data.remote.api

import com.example.tubesmobdev.data.remote.response.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>
    @Multipart
    @PATCH("api/profile")
    suspend fun updateProfile(
        @Part profilePhoto: MultipartBody.Part?,
        @Part("location") location: RequestBody?
    ): Response<ProfileResponse>
}
