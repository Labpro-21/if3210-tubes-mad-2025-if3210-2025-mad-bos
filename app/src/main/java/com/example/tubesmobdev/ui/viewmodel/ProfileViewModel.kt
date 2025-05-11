package com.example.tubesmobdev.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.local.preferences.ServicePreferences
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import com.example.tubesmobdev.data.repository.IAuthRepository
import com.example.tubesmobdev.data.repository.ProfileRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: IAuthRepository,
    private val songRepository: SongRepository,
    private val playerManager: PlayerManager,
    private val servicePreferences: ServicePreferences,
) : ViewModel() {

    // Profile state
    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Song counts
    private val _allSongsCount = MutableStateFlow(0)
    val allSongsCount: StateFlow<Int> = _allSongsCount.asStateFlow()

    private val _likedSongsCount = MutableStateFlow(0)
    val likedSongsCount: StateFlow<Int> = _likedSongsCount.asStateFlow()

    private val _listenedSongsCount = MutableStateFlow(0)
    val listenedSongsCount: StateFlow<Int> = _listenedSongsCount.asStateFlow()

    init {
        fetchProfile()
        fetchSongCounts()
    }

    private fun fetchSongCounts() {
        viewModelScope.launch {
            launch {
                songRepository.getAllSongsCount()
                    .collect { count ->
                        _allSongsCount.value = count
                    }
            }
            launch {
                songRepository.getLikedSongsCount()
                    .collect { count ->
                        _likedSongsCount.value = count
                    }
            }
            launch {
                songRepository.getPlayedSongsCount()
                    .collect { count ->
                        _listenedSongsCount.value = count
                    }
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = profileRepository.getProfile()
            result.fold(
                onSuccess = { _profile.value = it },
                onFailure = {
                    _errorMessage.value = it.message ?: "Unknown error"
                    Log.d("TokenRefreshService", it.message.toString())
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            servicePreferences.setShouldRestartService(false)
            authRepository.logout()
            playerManager.clearWithCallback()
        }
    }
}