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

    private fun insertSong(uri: Uri, title: String, artist: String, imageUri: Uri?, duration: Long) {
        viewModelScope.launch {
            val song = Song(
                title = title,
                artist = artist,
                filePath = uri.toString(),
                coverUrl = imageUri?.toString(),
                duration = duration,
                createdAt = System.currentTimeMillis(),
                isDownloaded = true
            )

            songRepository.insertSong(song)
        }
    }

    fun downloadAndInsertSong(
        context: Context,
        song: OnlineSong,
        onResult: (Result<Unit>) -> Unit
    ) {
        // If already downloading, don't start a new download
        if (_isDownloading.value) {
            onResult(Result.failure(Exception("Another download is already in progress")))
            return
        }

        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0f
            _currentDownloadTitle.value = song.title

            withContext(Dispatchers.IO) {
                try {
                    val resolver = context.contentResolver
                    val values = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, "${song.title}.mp3")
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                        put(MediaStore.Audio.Media.TITLE, song.title)
                        put(MediaStore.Audio.Media.ARTIST, song.artist)
                        put(MediaStore.Audio.Media.IS_MUSIC, 1)
                        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MyApp")
                    }

                    val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            val url = URL(song.url)
                            val connection = url.openConnection() as HttpURLConnection
                            connection.connect()

                            // Get file size if available
                            val fileLength = connection.contentLength

                            val inputStream = BufferedInputStream(connection.inputStream)
                            val buffer = ByteArray(8192) // Larger buffer for better performance
                            var bytesRead: Int
                            var totalBytesRead: Long = 0

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                // Update progress if file length is known
                                if (fileLength > 0) {
                                    val progress = totalBytesRead.toFloat() / fileLength.toFloat()
                                    withContext(Dispatchers.Main) {
                                        _downloadProgress.value = progress
                                    }
                                }
                            }

                            inputStream.close()
                            outputStream.flush()
                        }

                        withContext(Dispatchers.Main) {
                            insertSong(
                                uri,
                                song.title,
                                song.artist,
                                song.artwork.let { Uri.parse(it) },
                                parseDuration(song.duration)
                            )
                            _downloadProgress.value = 1f
                            _isDownloading.value = false
                            _currentDownloadTitle.value = ""
                            onResult(Result.success(Unit))
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _isDownloading.value = false
                            _currentDownloadTitle.value = ""
                            onResult(Result.failure(Exception("Failed to create MediaStore entry")))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Download", "Error downloading song", e)
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
}