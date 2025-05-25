package com.example.tubesmobdev.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.PlayerPreferencesRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.service.ConnectivityObserver
import com.example.tubesmobdev.service.ConnectivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository,
    private val playerRepository: PlayerPreferencesRepository,
    connectivityObserver: ConnectivityObserver,
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

    private val _downloadedSongs = MutableStateFlow<List<Song>>(emptyList())
    val downloadedSongs: StateFlow<List<Song>> = _downloadedSongs

    private val connectivityStatus: StateFlow<ConnectivityStatus> =
        connectivityObserver.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectivityStatus.Available
        )

    init {
        fetchSongs()
    }


    private fun fetchSongs() {
        viewModelScope.launch {
            launch {
                combine(repository.getAllSongs(), connectivityStatus) { songs, status ->
                    if (status == ConnectivityStatus.Unavailable) {
                        songs.filterNot { it.isOnline }
                    } else songs
                }.catch { e ->
                    _errorMessage.value = "Gagal memuat semua lagu: ${e.message}"
                }.collect {
                    _songs.value = it
                }
            }

            launch {
                combine(repository.getLikedSongs(), connectivityStatus) { songs, status ->
                    if (status == ConnectivityStatus.Unavailable) {
                        songs.filterNot { it.isOnline }
                    } else songs

                }.catch { e ->
                    _errorMessage.value = "Gagal memuat liked songs: ${e.message}"
                }.collect {
                    _likedSongs.value = it
                }
            }

            launch {
                repository.getDownloadedSongs()
                    .catch { e -> _errorMessage.value = "Gagal memuat downloaded songs: ${e.message}" }
                    .collect { _downloadedSongs.value = it }
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
                serverId = null,
                artist = artist,
                filePath = uri.toString(),
                coverUrl = imageUri?.toString(),
                duration = duration,
                createdAt = System.currentTimeMillis(),
                isDownloaded = false,
                isOnline = false,
            )

            val result = repository.insertSong(song)
            onResult(result)
        }

    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteSong(song: Song, context: Context) {
        viewModelScope.launch {
            val shouldDeleteDownloaded = song.isDownloaded

            if (shouldDeleteDownloaded) {
                try {
                    song.filePath.let { path ->
                        val uri = Uri.parse(path)
                        val deleted = context.contentResolver.delete(uri, null, null)
                        Log.d("SongDelete", "File deleted: $deleted")
                    }
                } catch (e: Exception) {
                    Log.w("SongDelete", "Gagal menghapus file audio: ${song.filePath}", e)
                }

                try {
                    song.coverUrl?.let { path ->
                        val uri = Uri.parse(path)
                        val deleted = context.contentResolver.delete(uri, null, null)
                        Log.d("SongDelete", "File deleted: $deleted")
                    }
                } catch (e: Exception) {
                    Log.d("SongDelete", "Gagal menghapus file artwork: ${song.coverUrl}", e)
                }
                repository.deleteSongDownloaded(song)
            } else {
                repository.deleteSong(song)
            }
            val queue = playerRepository.getQueue().first().toMutableList()
            val newQueue = queue.filter { it.id != song.id }
            playerRepository.saveQueue(newQueue)
        }
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            repository.updateSong(song)
            val queue = playerRepository.getQueue().first().toMutableList()
            val index = queue.indexOfFirst { it.id == song.id }
            queue[index] = song
            playerRepository.saveQueue(queue)
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