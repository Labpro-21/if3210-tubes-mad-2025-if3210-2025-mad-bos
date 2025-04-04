package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SongRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _newestSongs = MutableStateFlow<List<Song>>(emptyList())
    val newestSongs: StateFlow<List<Song>> get() = _newestSongs

    private val _recentlyPlayedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayedSongs: StateFlow<List<Song>> get() = _recentlyPlayedSongs

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        fetchNewestSongs()
        fetchRecentlyPlayedSongs()
    }

    private fun fetchNewestSongs() {
        viewModelScope.launch {
            repository.getNewestSongs()
                .catch { e -> _errorMessage.value = "Failed to fetch newest songs: ${e.message}" }
                .collect { _newestSongs.value = it }
        }
    }

    private fun fetchRecentlyPlayedSongs() {
        viewModelScope.launch {
            repository.getRecentlyPlayedSongs()
                .catch { e -> _errorMessage.value = "Failed to fetch recently played songs: ${e.message}" }
                .collect { _recentlyPlayedSongs.value = it }
        }
    }
}
