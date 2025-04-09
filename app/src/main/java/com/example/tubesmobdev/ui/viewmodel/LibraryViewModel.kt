package com.example.tubesmobdev.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository
): ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> get() = _songs

    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> get() = _likedSongs

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage
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
        }
    }

    fun updateLikeStatus(song: Song) {
        viewModelScope.launch {
            repository.updateLikedStatus(song.id, !song.isLiked)
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
}