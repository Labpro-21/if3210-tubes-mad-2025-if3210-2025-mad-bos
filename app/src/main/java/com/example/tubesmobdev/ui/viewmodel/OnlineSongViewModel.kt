package com.example.tubesmobdev.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.remote.response.OnlineSong
import com.example.tubesmobdev.data.remote.response.parseDuration
import com.example.tubesmobdev.data.remote.response.toLocalSong
import com.example.tubesmobdev.data.repository.OnlineSongRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.service.ConnectivityObserver
import com.example.tubesmobdev.service.ConnectivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(
    private val onlineSongRepository: OnlineSongRepository,
    private val songRepository: SongRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _songs = MutableStateFlow<List<OnlineSong>>(emptyList())
    val songs: StateFlow<List<OnlineSong>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _downloadedSongIds = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedSongIds: StateFlow<Set<Int>> = _downloadedSongIds


    private val _downloadingSongs = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val downloadingSongs: StateFlow<Map<Int, Boolean>> = _downloadingSongs

    private val connectivityStatus: StateFlow<ConnectivityStatus> =
        connectivityObserver.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectivityStatus.Available
        )

    init {
        viewModelScope.launch {
            songRepository.getDownloadedSongs().collect { songs ->
                _downloadedSongIds.value = songs.mapNotNull { it.serverId }.toSet()
            }
        }
    }

    private fun insertSong(serverId: Int, uri: Uri, title: String, artist: String, imageUri: Uri?, duration: Long) {
        viewModelScope.launch {
            val song = Song(
                serverId = serverId,
                title = title,
                artist = artist,
                filePath = uri.toString(),
                coverUrl = imageUri?.toString(),
                duration = duration,
                createdAt = System.currentTimeMillis(),
                isDownloaded = true,
                isOnline = false,
            )

            songRepository.insertSong(song)
        }
    }

    fun isDownloading(songId: Int): Boolean {
        return _downloadingSongs.value[songId] == true
    }

    private fun updateSong(song: Song) {
        viewModelScope.launch {
            songRepository.updateSong(song)
        }
    }

    fun convertToLocalSongs(songs: List<OnlineSong>, onResult: (List<Song>) -> Unit) {
        viewModelScope.launch {
            val result = songs.map { song ->
                val local = songRepository.findSongByServerId(song.id)
                local ?: song.toLocalSong()
            }
            onResult(result)
        }
    }

    suspend fun convertToLocalSong(song: OnlineSong): Song {
        val local = songRepository.findSongByServerId(song.id)
        return local ?: song.toLocalSong()
    }

    fun downloadAndInsertSong(
        context: Context,
        onlineSong: OnlineSong,
        onResult: (Result<Unit>) -> Unit
    ) {
        val songId = onlineSong.id
        if (isDownloading(songId)) {
            onResult(Result.failure(Exception("Lagu sedang diunduh.")))
            return
        }

        viewModelScope.launch {
            _downloadingSongs.update { it + (songId to true) }

            val existing = songRepository.findSongByServerId(songId)
            if (existing != null && existing.isDownloaded) {
                _downloadingSongs.update { it - songId }
                onResult(Result.failure(Exception("Lagu sudah diunduh sebelumnya.")))
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    val resolver = context.contentResolver

                    val audioValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, "${onlineSong.title}.mp3")
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                        put(MediaStore.Audio.Media.TITLE, onlineSong.title)
                        put(MediaStore.Audio.Media.ARTIST, onlineSong.artist)
                        put(MediaStore.Audio.Media.IS_MUSIC, 1)
                        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Purrytify")
                    }

                    val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioValues)
                        ?: throw Exception("Gagal membuat file audio di MediaStore")

                    resolver.openOutputStream(audioUri)?.use { outputStream ->
                        val connection = URL(onlineSong.url).openConnection() as HttpURLConnection
                        connection.connect()

                        val inputStream = BufferedInputStream(connection.inputStream)
                        val buffer = ByteArray(8192)
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (connectivityStatus.value != ConnectivityStatus.Available) {
                                inputStream.close()
                                outputStream.close()
                                resolver.delete(audioUri, null, null)
                                throw Exception("Koneksi terputus, download dibatalkan.")
                            }
                            outputStream.write(buffer, 0, bytesRead)
                        }

                        inputStream.close()
                        outputStream.flush()
                    }

                    val artworkUri = run {
                        val imageValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "${onlineSong.title}_artwork.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Purrytify")
                        }

                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageValues)
                        try {
                            if (uri != null) {
                                resolver.openOutputStream(uri)?.use { outputStream ->
                                    val connection = URL(onlineSong.artwork).openConnection() as HttpURLConnection
                                    connection.connect()
                                    val inputStream = BufferedInputStream(connection.inputStream)
                                    val buffer = ByteArray(8192)
                                    var bytes: Int
                                    while (inputStream.read(buffer).also { bytes = it } != -1) {
                                        if (connectivityStatus.value != ConnectivityStatus.Available) {
                                            inputStream.close()
                                            outputStream.close()
                                            resolver.delete(uri, null, null)
                                            resolver.delete(audioUri, null, null)
                                            throw Exception("Koneksi terputus saat mengunduh artwork.")
                                        }
                                        outputStream.write(buffer, 0, bytes)
                                    }
                                    inputStream.close()
                                    outputStream.flush()
                                }
                            }
                            uri
                        } catch (e: Exception) {
                            if (uri != null) resolver.delete(uri, null, null)
                            resolver.delete(audioUri, null, null)
                            throw e
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (existing == null) {
                            insertSong(
                                serverId = songId,
                                uri = audioUri,
                                title = onlineSong.title,
                                artist = onlineSong.artist,
                                imageUri = artworkUri,
                                duration = parseDuration(onlineSong.duration)
                            )
                        } else {
                            updateSong(
                                existing.copy(
                                    filePath = audioUri.toString(),
                                    title = onlineSong.title,
                                    artist = onlineSong.artist,
                                    coverUrl = artworkUri?.toString(),
                                    duration = parseDuration(onlineSong.duration),
                                    isOnline = false,
                                    isDownloaded = true
                                )
                            )
                        }
                        onResult(Result.success(Unit))
                    }
                } catch (e: Exception) {
                    Log.e("Download", "Error", e)
                    withContext(Dispatchers.Main) {
                        onResult(Result.failure(e))
                    }
                } finally {
                    _downloadingSongs.update { it - songId }
                }
            }
        }
    }

    fun fetchSongs(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = if (code.lowercase() == "global") {
                    onlineSongRepository.getTopGlobalSongs()
                } else {
                    onlineSongRepository.getTopSongsByCountry(code)
                }
                _songs.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getOnlineSongById(id: String): OnlineSong {
        return onlineSongRepository.getOnlineSong(id)
    }
}