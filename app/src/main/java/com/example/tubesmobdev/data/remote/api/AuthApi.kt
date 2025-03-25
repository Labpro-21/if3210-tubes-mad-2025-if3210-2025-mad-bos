package com.example.tubesmobdev.data.remote.api

import com.example.tubesmobdev.data.remote.request.LoginRequest
import com.example.tubesmobdev.data.remote.request.RefreshTokenRequest
import com.example.tubesmobdev.data.remote.response.LoginResponse
import com.example.tubesmobdev.data.remote.response.TokenResponse
import com.example.tubesmobdev.data.remote.response.VerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/refresh-token")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<TokenResponse>

    @GET("api/verify-token")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<VerifyResponse>
}