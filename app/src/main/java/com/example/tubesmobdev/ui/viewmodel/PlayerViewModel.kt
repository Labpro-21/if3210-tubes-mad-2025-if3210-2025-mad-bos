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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlin.math.log

@HiltViewModel
class PlayerViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val app: Application,
    private val songRepository: SongRepository,
    private val listeningRecordRepository: ListeningRecordRepository,
    private val playerRepository: PlayerPreferencesRepository,
    private val playerManager: PlayerManager,
    private val playbackConnection: PlaybackConnection
) : AndroidViewModel(app) {

    private var listeningStartTime: Long? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    val songList: StateFlow<List<Song>> = _songList

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue

    private var currentQueueIndex = -1

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress



    init {
        observeQueue()
        observeSongs()
        observePlaybackState()
        observeSongEvents()

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
                                    songRepository.updateLikedStatus(song.id, newLikedState)
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
                }
            }
        }
    }


    @OptIn(UnstableApi::class)
    private fun observePlaybackState() {
        viewModelScope.launch {
            val controller = playbackConnection.getController()
            while (true) {
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

    private fun observeSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs().collect { songs ->
                _songList.value = songs
                validateQueueWithSongs(songs)
            }
        }
    }

    private fun observeQueue() {
        viewModelScope.launch {
            playerRepository.getQueue().collect { queue ->
                _currentQueue.value = queue
            }
        }
    }

    private fun validateQueueWithSongs(songs: List<Song>) {
        val currentQueue = _currentQueue.value
        if (currentQueue.isEmpty()) return

        val validQueue = currentQueue.filter { queueSong ->
            songs.any { it.id == queueSong.id }
        }

        if (currentQueue.size != validQueue.size) {
            Log.d("PlayerViewModel", "Invalid songs removed from queue")
            updateQueue(validQueue)

            if (currentQueueIndex >= validQueue.size) {
                currentQueueIndex = validQueue.size - 1
            }
        }
    }

    private fun onSongChanged(song: Song) {
        viewModelScope.launch {
            saveListeningDuration()

            _currentSong.value = song
            listeningStartTime = System.currentTimeMillis()

            if (!song.isOnline) {
                songRepository.updateLastPlayed(song.id, System.currentTimeMillis())
            }

            currentQueueIndex = _currentQueue.value.indexOfFirst { it.id == song.id }
                .takeIf { it != -1 } ?: currentQueueIndex
        }
    }

    private suspend fun saveListeningDuration() {
        val song = _currentSong.value ?: return
        val startTime = listeningStartTime ?: return
        val endTime = System.currentTimeMillis()
        val listenedMs = endTime - startTime

        if (listenedMs >= 5000 && !song.isOnline) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val record = ListeningRecord(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                creatorId = song.creatorId,
                date = today,
                durationListened = listenedMs
            )
            listeningRecordRepository.insertRecord(record)
        }

        listeningStartTime = null
    }

    fun playSong(song: Song) {
        _currentSong.value = song

        val queue = _currentQueue.value.ifEmpty { _songList.value }

        playerManager.play(
            song = song,
            queue = queue,
            isShuffle = _isShuffle.value,
            repeatMode = _repeatMode.value
        )

        if (!song.isOnline) {
            viewModelScope.launch {
                songRepository.updateLastPlayed(song.id, System.currentTimeMillis())
            }
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
        playerManager.clearWithCallback {
            _currentSong.value = null
        }
    }

    fun clearCurrentQueue() {
        _currentQueue.value = emptyList()
        observeQueue()
        observeSongs()
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
        _currentQueue.value = queue
        viewModelScope.launch {
            playerRepository.saveQueue(queue)
        }
    }

    fun setCurrentQueue(queue: List<Song>) {
        _currentQueue.value = queue
        updateQueue(queue)
    }

    fun addQueue(newSong: Song) {
        val updatedQueue = _currentQueue.value.toMutableList()
        updatedQueue.add(newSong)
        _currentQueue.value = updatedQueue
        updateQueue(updatedQueue)
    }

    fun shareSong(song: Song) {
        val songUri = "purrytify://song/${song.id}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Dengarkan lagu favoritku di Purrytify!\n$songUri")
        }
        app.applicationContext.startActivity(
            Intent.createChooser(shareIntent, "Bagikan lagu melalui").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}