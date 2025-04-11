package com.example.tubesmobdev.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import com.example.tubesmobdev.data.repository.IAuthRepository
import com.example.tubesmobdev.data.repository.ProfileRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: IAuthRepository,
    private val songRepository: SongRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    var profile: ProfileResponse? by mutableStateOf(null)
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set
    var allSongsCount by mutableStateOf(0)
        private set

    var likedSongsCount by mutableStateOf(0)
        private set
    var listenedSongsCount by mutableStateOf(0)
        private set

    init {
        fetchProfile()
        fetchSongCounts()
    }

    private fun fetchSongCounts() {
        viewModelScope.launch {
            launch {
                songRepository.getAllSongsCount()
                    .collect { count ->
                        allSongsCount = count
                    }
            }
            launch {
                songRepository.getLikedSongsCount()
                    .collect { count ->
                        likedSongsCount = count
                    }
            }
            launch {
                songRepository.getPlayedSongsCount()
                    .collect { count ->
                        listenedSongsCount = count
                    }
            }
        }
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
            playerManager.clearWithCallback()
        }
    }
}
