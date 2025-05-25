package com.example.tubesmobdev.data.repository

import android.content.Context
import android.net.Uri
import com.example.tubesmobdev.data.local.preferences.IProfilePreferences
import com.example.tubesmobdev.data.remote.api.ProfileApi
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val profilePreferences: IProfilePreferences
) {
    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = profileApi.getProfile()
            if (response.isSuccessful) {
                val profile = response.body()
                if (profile != null) {
                    profilePreferences.saveProfile(profile)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Empty profile response"))
                }
            } else {
                fallbackToCachedProfile("Fetch failed with code ${response.code()}")
            }
        } catch (e: Exception) {
            fallbackToCachedProfile(e.message ?: "Unknown error")
        }
    }

    private suspend fun fallbackToCachedProfile(reason: String): Result<ProfileResponse> {
        val cached = profilePreferences.getCachedProfile()
        return if (cached != null) {
            Result.success(cached)
        } else {
            Result.failure(Exception("No cached profile available. Reason: $reason"))
        }
    }

    suspend fun updateProfilePhoto(context: Context, uri: Uri): Result<ProfileResponse> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = MultipartBody.Part.createFormData(
                "profilePhoto",
                tempFile.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
            )
            val locationPart = RequestBody.create("text/plain".toMediaTypeOrNull(), "")

            val response = profileApi.updateProfile(
                profilePhoto = requestFile,
                location = locationPart
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    profilePreferences.saveProfile(it)
                    Result.success(it)
                } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLocation(location: String): Result<ProfileResponse> {
        return try {
            val locationPart = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                location
            )
            val response = profileApi.updateProfile(
                profilePhoto = null,
                location = locationPart
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    profilePreferences.saveProfile(it)
                    Result.success(it)
                } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}