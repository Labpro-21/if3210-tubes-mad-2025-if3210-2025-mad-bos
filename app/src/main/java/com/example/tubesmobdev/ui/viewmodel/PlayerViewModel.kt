package com.example.tubesmobdev.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import android.graphics.Bitmap
import android.content.ContentValues
import android.provider.MediaStore
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.ListeningRecordRepository
import com.example.tubesmobdev.data.repository.PlayerPreferencesRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.manager.PlaybackConnection
import com.example.tubesmobdev.manager.PlayerManager
import com.example.tubesmobdev.service.MusicService
import com.example.tubesmobdev.util.RepeatMode
import com.example.tubesmobdev.util.SongEvent
import com.example.tubesmobdev.util.SongEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.local.preferences.PlayerPreferences
import com.example.tubesmobdev.data.model.toMediaItem
import com.example.tubesmobdev.service.generateQRCodeUrl
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import java.io.File

@HiltViewModel
class PlayerViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val app: Application,
    private val songRepository: SongRepository,
    private val playerRepository: PlayerPreferencesRepository,
    private val playerManager: PlayerManager,
    private val playbackConnection: PlaybackConnection,
    private val playerPreferences: PlayerPreferences
) : AndroidViewModel(app) {
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext

    private val _hasPrev = MutableStateFlow(false)
    val hasPrev: StateFlow<Boolean> = _hasPrev

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage

    init {
        observePlaybackState()
        observeSongEvents()
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {

        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error unregistering receiver", e)
        }
    }

    private fun observeSongEvents() {
        viewModelScope.launch {
            SongEventBus.events.collect { event ->
                when (event) {
                    is SongEvent.SongChanged -> {
                        Log.d("PlayerViewModel", "Song changed: ${event.song.title}")
                        onSongChanged(event.song)
                    }
                    is SongEvent.SongLiked -> {
                        Log.d("PlayerViewModel", "Song liked toggle: id=${event.songId}")
                        if (_currentSong.value?.id == event.songId) {
                            currentSong.value?.let { song ->
                                val newLikedState = event.isLiked
                                _currentSong.value = song.copy(isLiked = newLikedState)
                                viewModelScope.launch {
                                    songRepository.updateLikedStatus(song.copy(isLiked = newLikedState))
                                }
                            }
                        }
                    }
                    is SongEvent.ShuffleToggled -> {
                        Log.d("PlayerViewModel", "Shuffle toggled")
                        _isShuffle.value = event.isShuffle
                    }
                    is SongEvent.RepeatToggled -> {
                        Log.d("PlayerViewModel", "Repeat toggled")
                        val repeatMode = event.repeatMode
                        _repeatMode.value = when (repeatMode) {
                            0 -> RepeatMode.NONE
                            1 -> RepeatMode.REPEAT_ONE
                            2 -> RepeatMode.REPEAT_ALL
                            else -> RepeatMode.NONE
                        }
                    }

                    is SongEvent.SongPaused -> {
                        Log.d("PlayerViewModel", "Song paused: ${event.song}")
                    }
                    is SongEvent.SongSeeked -> {
                        Log.d("PlayerViewModel", "Song seeked: ${event.song}, from: ${event.from}, to: ${event.to}")
                    }
                    is SongEvent.SongStarted -> {
                        Log.d("PlayerViewModel", "Song started: ${event.song}")
                    }
                }
            }
        }
    }


    @OptIn(UnstableApi::class)
    private fun observePlaybackState() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            while (controller.isConnected) {
                _isPlaying.value = controller.isPlaying
                val duration = controller.duration.coerceAtLeast(1L)
                val position = controller.currentPosition
                _progress.value = position.toFloat() / duration
                delay(500)
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun seekToPosition(progress: Float) {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            val duration = controller.duration.coerceAtLeast(1L)

            controller.seekTo((progress * duration).toLong())
        }
    }


    @OptIn(UnstableApi::class)
    fun checkHasNext() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            _hasNext.value = controller.hasNextMediaItem()
        }
    }

    @OptIn(UnstableApi::class)
    fun checkHasPrev() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            _hasPrev.value = controller.hasPreviousMediaItem()
        }
    }


    private fun onSongChanged(song: Song) {
        viewModelScope.launch {
            _currentSong.value = song
            songRepository.updateLastPlayed(song, System.currentTimeMillis())
            checkHasNext()
            checkHasPrev()
        }
    }

    fun playSong(song: Song) {
        val currentQueue = _currentQueue.value
        val isInQueue = currentQueue.any { it.id == song.id }

        val queue = if (isInQueue) currentQueue else listOf(song)

        _currentQueue.value = queue

        val uri = Uri.parse(song.filePath)
        val exists = if (uri.scheme == "content") {
            isContentUriExists(app.applicationContext, uri)
        } else {
            File(song.filePath).exists()
        }



        viewModelScope.launch {

            if (!song.isOnline && !exists){
                showToast("File song not exist!!")
                return@launch
            }
            playerManager.play(
                song = song,
                queue = queue,
                isShuffle = _isShuffle.value,
                repeatMode = _repeatMode.value,
            )

            songRepository.updateLastPlayed(song, System.currentTimeMillis())
            onSongChanged(song)
        }

    }

    private fun isContentUriExists(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    @OptIn(UnstableApi::class)
    fun togglePlayPause() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    private fun clearSong() {
        viewModelScope.launch {
            playerManager.clearWithCallback {
                _currentSong.value = null
            }
        }
    }

     fun updateCurrentSongAfterDownload() {
        viewModelScope.launch {
            val song = _currentSong.value
            if (song != null) {
                val newSong = song.serverId?.let { songRepository.findSongByServerId(it) }
                _currentSong.value = newSong
            }
        }
    }

    fun clearCurrentQueue() {
        _currentQueue.value = emptyList()
    }

    fun stopIfPlaying(song: Song) {
        if (_currentSong.value?.id == song.id) {
            clearSong()
        }
    }

    @OptIn(UnstableApi::class)
    fun toggleLike() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            controller.sendCustomCommand(SessionCommand(MusicService.ACTION_TOGGLE_LIKE, Bundle.EMPTY), Bundle.EMPTY)
        }
    }


    @OptIn(UnstableApi::class)
    fun toggleShuffle() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            controller.sendCustomCommand(SessionCommand(MusicService.ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY), Bundle.EMPTY)
        }
    }

    @OptIn(UnstableApi::class)
    fun cycleRepeatMode() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            controller.sendCustomCommand(SessionCommand(MusicService.ACTION_TOGGLE_REPEAT, Bundle.EMPTY), Bundle.EMPTY)
        }
    }

    @OptIn(UnstableApi::class)
    fun playNext() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            controller.seekToNext()
        }
    }

    @OptIn(UnstableApi::class)
    fun playPrevious() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            controller.seekToPrevious()
        }
    }

    fun updateCurrentSongIfMatches(updatedSong: Song) {
        val current = _currentSong.value
        if (current != null && current.id == updatedSong.id) {
            val updatedWithOnlineStatus = updatedSong.copy()
            updatedWithOnlineStatus.isOnline = current.isOnline
            _currentSong.value = updatedWithOnlineStatus
        }
    }

    private fun updateQueue(queue: List<Song>) {
        viewModelScope.launch {
            playerRepository.saveQueue(queue)
        }
    }

    fun setCurrentQueue(queue: List<Song>) {
        _currentQueue.value = queue
    }

    fun addQueue(newSong: Song) {
        viewModelScope.launch {
            val updatedQueue = playerRepository.getQueue().first().toMutableList()
            updatedQueue.add(newSong)
            _currentQueue.value = updatedQueue
            updateQueue(updatedQueue)
        }
    }

    fun removeSongFromQueue(songId: Int){
        viewModelScope.launch {
            val updatedQueue = playerRepository.getQueue().first().toMutableList()
            val newQueue = updatedQueue.filter { it.id != songId }
            _currentQueue.value = newQueue
            updateQueue(newQueue)
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            playerRepository.clearQueue()
        }
    }

    fun shareSong(song: Song) {
        val songUri = "purrytify://song/${song.serverId}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Dengarkan lagu favoritku di Purrytify!\n$songUri")
        }
        app.applicationContext.startActivity(
            Intent.createChooser(shareIntent, "Bagikan lagu melalui").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }



    fun shareQRCode(context: Context, song: Song) {
        try {
            val bitmap = generateQRCodeUrl(song)

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "qr_${song.serverId}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.WIDTH, bitmap.width)
                put(MediaStore.Images.Media.HEIGHT, bitmap.height)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QR")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to share QR code", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(UnstableApi::class)
    fun restoreLastSession() {
        viewModelScope.launch {
            val queue = playerPreferences.getLastQueue.first()
            val song = playerPreferences.getLastPlayedSong.first()
            val position = playerPreferences.getLastPosition.first()

            Log.d("Restore", queue.toString())
            Log.d("Restore", song.toString())
            Log.d("Restore", position.toString())

            if (queue.isNotEmpty() && song != null) {
                _currentQueue.value = queue
                _currentSong.value = song
                _isPlaying.value = false
                _progress.value = position / (song.duration.toFloat().coerceAtLeast(1f))

                val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                val controller = playbackConnection.getController()

                val jsonQueue = Gson().toJson(queue)
                val args = Bundle().apply {
                    putString("queue", jsonQueue)
                }
                val mediaItems = queue.map { it.toMediaItem() }
                controller.setMediaItems(mediaItems, index, 0)
                controller.prepare()
                controller.sendCustomCommand(SessionCommand(MusicService.SONG_QUEUE, Bundle.EMPTY), args)
                controller.seekTo(index, position)
                controller.pause()
            }

            playerPreferences.clearLastSession()
        }
    }
}