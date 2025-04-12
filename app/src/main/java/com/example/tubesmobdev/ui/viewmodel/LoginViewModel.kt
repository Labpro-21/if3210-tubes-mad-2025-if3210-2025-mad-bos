package com.example.tubesmobdev.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.repository.IAuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.service.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {
    private val _loginErrorMessage = MutableStateFlow<String?>(null)
    val loginErrorMessage = _loginErrorMessage.asStateFlow()


    fun login(
        email: String,
        password: String,
//        navController: NavController
    ) {
        if (email.isBlank()) {
            _loginErrorMessage.value = "Email harus diisi"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginErrorMessage.value = "Format email tidak valid"
            return
        }
        if (password.isBlank()) {
            _loginErrorMessage.value = "Password harus diisi"
            return
        }
        viewModelScope.launch {
            _loginErrorMessage.value = null
            authRepository.login(email, password)
                .fold(
                    onSuccess = { authResult ->
                        when (authResult) {
                            is AuthResult.Success -> {
//                                navController.navigate("main") {
//                                    popUpTo("login") { inclusive = true }
//                                }
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
