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
import com.example.tubesmobdev.data.repository.OnlineSongRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.service.ConnectivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(
    private val onlineSongRepository: OnlineSongRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<OnlineSong>>(emptyList())
    val songs: StateFlow<List<OnlineSong>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    private val _currentDownloadTitle = MutableStateFlow("")
    val currentDownloadTitle: StateFlow<String> = _currentDownloadTitle

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

    private fun updateSong(song: Song) {
        viewModelScope.launch {
            songRepository.updateSong(song)
        }
    }

    fun downloadAndInsertSong(
        context: Context,
        onlineSong: OnlineSong,
        connectivityStatus: StateFlow<ConnectivityStatus>,
        onResult: (Result<Unit>) -> Unit
    ) {
        if (_isDownloading.value) {
            onResult(Result.failure(Exception("Another download is already in progress")))
            return
        }

        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0f
            _currentDownloadTitle.value = onlineSong.title

            val existing = songRepository.findSongByServerId(onlineSong.id)
            if (existing != null && existing.isDownloaded) {
                _isDownloading.value = false
                _currentDownloadTitle.value = ""
                onResult(Result.failure(Exception("This song has already been downloaded")))
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
                        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MyApp")
                    }
                    val audioUri =
                        resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioValues)
                            ?: throw Exception("Failed to create MediaStore entry for audio")

                    resolver.openOutputStream(audioUri)?.use { outputStream ->
                        val url = URL(onlineSong.url)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connect()

                        val fileLength = connection.contentLength
                        val inputStream = BufferedInputStream(connection.inputStream)
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (connectivityStatus.value != ConnectivityStatus.Available) {
                                inputStream.close()
                                outputStream.close()
                                resolver.delete(audioUri, null, null)
                                throw Exception("Koneksi internet terputus. Download dibatalkan.")
                            }

                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (fileLength > 0) {
                                val progress = totalBytesRead.toFloat() / fileLength
                                withContext(Dispatchers.Main) {
                                    _downloadProgress.value = progress * 0.8f
                                }
                            }
                        }

                        inputStream.close()
                        outputStream.flush()
                    }

                    val artworkUri = run {
                        val imageValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "${onlineSong.title}_artwork.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
                        }

                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageValues)
                        try {
                            if (uri != null) {
                                resolver.openOutputStream(uri)?.use { outputStream ->
                                    val connection = URL(onlineSong.artwork).openConnection() as HttpURLConnection
                                    connection.connect()
                                    val inputStream = BufferedInputStream(connection.inputStream)
                                    val buffer = ByteArray(8192)
                                    var bytesRead: Int
                                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                        if (connectivityStatus.value != ConnectivityStatus.Available) {
                                            inputStream.close()
                                            outputStream.close()
                                            resolver.delete(uri, null, null)
                                            resolver.delete(audioUri, null, null)
                                            throw Exception("Koneksi internet terputus saat mengunduh artwork. Lagu dibatalkan.")
                                        }
                                        outputStream.write(buffer, 0, bytesRead)
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
                        if (existing == null){
                            insertSong(
                                onlineSong.id,
                                audioUri,
                                onlineSong.title,
                                onlineSong.artist,
                                artworkUri,
                                parseDuration(onlineSong.duration)
                            )
                        } else {
                            val updatedSong = existing.copy(
                                filePath = audioUri.toString(),
                                title = onlineSong.title,
                                artist = onlineSong.artist,
                                coverUrl = artworkUri.toString(),
                                duration = parseDuration(onlineSong.duration),
                                isOnline = false,
                                isDownloaded = true
                            )

                            updateSong(updatedSong)
                        }
                        _downloadProgress.value = 1f
                        _isDownloading.value = false
                        _currentDownloadTitle.value = ""
                        onResult(Result.success(Unit))
                    }
                } catch (e: Exception) {
                    Log.e("Download", "Error downloading song/artwork", e)
                    withContext(Dispatchers.Main) {
                        _isDownloading.value = false
                        _currentDownloadTitle.value = ""
                        onResult(Result.failure(e))
                    }
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