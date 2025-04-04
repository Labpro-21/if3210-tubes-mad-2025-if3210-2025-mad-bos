package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.remote.api.ProfileApi
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi
) {
    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = profileApi.getProfile()
            if (response.isSuccessful) {
                val profile = response.body()
                if (profile != null) {
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Empty profile response"))
                }
            } else {
                Result.failure(Exception("Profile fetch failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
