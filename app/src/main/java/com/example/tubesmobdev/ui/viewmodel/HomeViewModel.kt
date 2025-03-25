package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tubesmobdev.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun logout(navController: NavController) {
        viewModelScope.launch {

            authRepository.logout()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }
}