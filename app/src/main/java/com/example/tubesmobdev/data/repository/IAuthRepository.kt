package com.example.tubesmobdev.data.repository

import com.example.tubesmobdev.domain.model.AuthResult


interface IAuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun refreshToken(): Result<AuthResult>
    suspend fun verifyToken(): Result<AuthResult>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
}