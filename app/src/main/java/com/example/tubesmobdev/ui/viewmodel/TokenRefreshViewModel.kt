package com.example.tubesmobdev.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.manager.PlayerManager
import com.example.tubesmobdev.service.ConnectivityObserver
import com.example.tubesmobdev.service.ConnectivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenRefreshViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val playerManager: PlayerManager,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private var tokenJob: Job? = null

    fun startTokenRefreshLoop() {
        if (tokenJob?.isActive == true) return

        tokenJob = viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                if (status is ConnectivityStatus.Available) {
                    checkTokenValidity()
                } else {
                    Log.w("TokenRefreshLoop", "Skip token check, No internet")
                }

                delay(3 * 60 * 1000L)
            }
        }
    }

    fun stopTokenRefreshLoop() {
        tokenJob?.cancel()
        tokenJob = null
    }

    private suspend fun checkTokenValidity() {
        authRepository.verifyToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshLoop", "Token is valid")
                    is AuthResult.TokenExpired -> refreshToken()
                    is AuthResult.Failure -> forceLogout()
                }
            },
            onFailure = {
                Log.e("TokenRefreshLoop", "Token check error", it)
            }
        )
    }

    private suspend fun refreshToken() {
        authRepository.refreshToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshLoop", "Token refreshed successfully")
                    is AuthResult.TokenExpired, is AuthResult.Failure -> forceLogout()
                }
            },
            onFailure = {
                Log.e("TokenRefreshLoop", "Token refresh error", it)
            }
        )
    }

    private suspend fun forceLogout() {
        authRepository.logout()
        playerManager.clearWithCallback()
    }
}
