package com.example.tubesmobdev.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import com.example.tubesmobdev.data.repository.ProfileRepository
import com.example.tubesmobdev.data.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository, // Injecting concrete class directly
    private val authRepository: IAuthRepository
) : ViewModel() {

    var profile: ProfileResponse? by mutableStateOf(null)
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = profileRepository.getProfile()
            result.fold(
                onSuccess = { profile = it },
                onFailure = { errorMessage = it.message ?: "Unknown error" }
            )
            isLoading = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
