package com.example.tubesmobdev.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenRefreshViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun startTokenRefreshLoop() {
        if (_isRunning.value) return // biar nggak double loop

        _isRunning.value = true
        viewModelScope.launch {
            while (isActive && _isRunning.value) {
                Log.d("TokenRefreshLoop", "Checking token validity...")
                checkTokenValidity()
                delay(3 * 60 * 1000L) // 3 menit
            }
        }
    }

    fun stopTokenRefreshLoop() {
        _isRunning.value = false
    }

    private suspend fun checkTokenValidity() {
        authRepository.verifyToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshLoop", "Token is valid")
                    is AuthResult.TokenExpired -> refreshToken()
                    is AuthResult.Failure -> {
                        Log.e("TokenRefreshLoop", "Token invalid → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshLoop", "Token check error", e)
            }
        )
    }

    private suspend fun refreshToken() {
        authRepository.refreshToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshLoop", "Token refreshed successfully")
                    is AuthResult.TokenExpired, is AuthResult.Failure -> {
                        Log.e("TokenRefreshLoop", "Token expired or failed → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshLoop", "Token refresh error", e)
            }
        )
    }
}
