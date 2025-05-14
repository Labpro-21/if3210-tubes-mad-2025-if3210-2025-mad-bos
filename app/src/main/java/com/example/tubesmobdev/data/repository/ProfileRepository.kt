package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.data.remote.api.ProfileApi
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import android.net.Uri
import android.content.Context
import java.io.File

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

            val response = profileApi.updateProfilePhoto(requestFile)

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
