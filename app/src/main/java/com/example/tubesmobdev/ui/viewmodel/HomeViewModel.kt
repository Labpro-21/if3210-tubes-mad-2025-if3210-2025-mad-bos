package com.example.tubesmobdev.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.service.ConnectivityObserver
import com.example.tubesmobdev.service.ConnectivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SongRepository,
    private val authRepository: AuthRepository,
    connectivityObserver: ConnectivityObserver,
    ) : ViewModel() {

    private val _newestSongs = MutableStateFlow<List<Song>>(emptyList())
    val newestSongs: StateFlow<List<Song>> get() = _newestSongs

    private val _recentlyPlayedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayedSongs: StateFlow<List<Song>> get() = _recentlyPlayedSongs

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> get() = _allSongs

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val connectivityStatus: StateFlow<ConnectivityStatus> =
        connectivityObserver.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectivityStatus.Available
        )

    init {
        fetchNewestSongs()
        fetchRecentlyPlayedSongs()
        fetchAllSongs()
    }

    private fun fetchNewestSongs() {
        viewModelScope.launch {
            combine(repository.getNewestSongs(), connectivityStatus) { songs, status ->
                if (status == ConnectivityStatus.Unavailable) {
                    songs.filterNot { it.isOnline }
                } else songs
            }.catch { e ->
                _errorMessage.value = "Failed to fetch newest songs: ${e.message}"
            }.collect {
                _newestSongs.value = it
            }
        }
    }

    private fun fetchRecentlyPlayedSongs() {
        viewModelScope.launch {
            combine(repository.getRecentlyPlayedSongs(), connectivityStatus) { songs, status ->
                if (status == ConnectivityStatus.Unavailable) {
                    songs.filterNot { it.isOnline }
                } else songs
            }.catch { e ->
                _errorMessage.value = "Failed to fetch recently played songs: ${e.message}"
            }.collect {
                _recentlyPlayedSongs.value = it
            }
        }
    }

    private fun fetchAllSongs() {
        viewModelScope.launch {
            combine(repository.getAllSongs(), connectivityStatus) { songs, status ->
                if (status == ConnectivityStatus.Unavailable) {
                    songs.filterNot { it.isOnline }
                } else songs
            }.catch { e ->
                _errorMessage.value = "Failed to fetch recently played songs: ${e.message}"
            }.collect {
                _allSongs.value = it
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
