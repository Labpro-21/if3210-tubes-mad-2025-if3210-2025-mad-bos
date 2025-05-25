package com.example.tubesmobdev.data.local.preferences

import com.example.tubesmobdev.data.remote.response.ProfileResponse

interface IProfilePreferences {
    suspend fun saveProfile(profile: ProfileResponse)
    suspend fun getCachedProfile(): ProfileResponse?
    suspend fun clearProfile()
}