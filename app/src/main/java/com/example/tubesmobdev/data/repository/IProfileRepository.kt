package com.example.tubesmobdev.data.repository

import android.content.Context
import android.net.Uri
import com.example.tubesmobdev.data.remote.response.ProfileResponse

interface IProfileRepository {
    suspend fun getProfile(): Result<ProfileResponse>

    suspend fun updateProfilePhoto(context: Context, uri: Uri): Result<ProfileResponse>
}
