package com.example.tubesmobdev.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _loginErrorMessage = MutableStateFlow<String?>(null)
    val loginErrorMessage = _loginErrorMessage.asStateFlow()

    fun login(email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            _loginErrorMessage.value = null
            authRepository.login(email, password)
                .fold(
                    onSuccess = { authResult ->
                        when (authResult) {
                            is AuthResult.Success -> {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is AuthResult.Failure -> {
                                _loginErrorMessage.value = authResult.message
                            }
                            AuthResult.TokenExpired -> {
                                _loginErrorMessage.value = "Sesi telah berakhir. Silakan login kembali."
                            }
                        }
                    },
                    onFailure = { exception ->
                        _loginErrorMessage.value = "Terjadi kesalahan: ${exception.message}"
                    }
                )
        }
    }
}
