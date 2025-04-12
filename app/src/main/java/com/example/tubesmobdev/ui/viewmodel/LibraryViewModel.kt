package com.example.tubesmobdev.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.PlayerPreferencesRepository
import com.example.tubesmobdev.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository,
    private val playerRepository: PlayerPreferencesRepository,
): ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> get() = _songs

    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> get() = _likedSongs

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue

    init {
        fetchSongs()
    }


    private fun fetchSongs() {
        viewModelScope.launch {
            launch {
                repository.getAllSongs()
                    .catch { e -> _errorMessage.value = "Gagal memuat semua lagu: ${e.message}" }
                    .collect { _songs.value = it }
            }

            launch {
                repository.getLikedSongs()
                    .catch { e -> _errorMessage.value = "Gagal memuat liked songs: ${e.message}" }
                    .collect { _likedSongs.value = it }
            }
            launch {
                playerRepository.getQueue().collect { queue ->
                    _currentQueue.value = queue
                }
            }
        }
    }


    fun insertSong(uri: Uri, title: String, artist: String, imageUri: Uri?, duration: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val song = Song(
                title = title,
                artist = artist,
                filePath = uri.toString(),
                coverUrl = imageUri?.toString(),
                duration = duration,
                createdAt = System.currentTimeMillis()
            )

            val result = repository.insertSong(song)
            onResult(result)
        }

    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song)
        }
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            repository.updateSong(song)
        }
    }

    fun searchSongs(query: String) {
        viewModelScope.launch {
            repository.searchSongs(query).collect { results ->
                _searchResults.value = results
            }
        }
    }
}