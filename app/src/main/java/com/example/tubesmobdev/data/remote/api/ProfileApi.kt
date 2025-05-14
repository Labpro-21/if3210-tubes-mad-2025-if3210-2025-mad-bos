package com.example.tubesmobdev.data.remote.api

import com.example.tubesmobdev.data.remote.response.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import okhttp3.MultipartBody
import retrofit2.http.*

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>
    @Multipart
    @PATCH("api/profile")
    suspend fun updateProfilePhoto(
        @Part profilePhoto: MultipartBody.Part
    ): Response<ProfileResponse>
}
