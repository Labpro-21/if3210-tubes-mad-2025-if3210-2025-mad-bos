package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.remote.response.ProfileResponse

interface IProfileRepository {
    suspend fun getProfile(): Result<ProfileResponse>
}
