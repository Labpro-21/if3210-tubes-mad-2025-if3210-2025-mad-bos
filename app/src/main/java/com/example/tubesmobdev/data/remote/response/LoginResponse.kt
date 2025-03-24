package com.example.tubesmobdev.data.remote.response

import com.example.tubesmobdev.domain.model.User

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
)
