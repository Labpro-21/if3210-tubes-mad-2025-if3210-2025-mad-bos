package com.example.tubesmobdev.domain.model

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val message: String) : AuthResult()
    object TokenExpired : AuthResult()
}