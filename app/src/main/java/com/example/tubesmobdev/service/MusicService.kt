package com.example.tubesmobdev.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.tubesmobdev.MainActivity
import com.example.tubesmobdev.R
import com.example.tubesmobdev.data.local.dao.ListeningRecordDao
import com.example.tubesmobdev.data.local.preferences.AuthPreferences
import com.example.tubesmobdev.data.local.preferences.PlayerPreferences
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.ListeningSession
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.model.toMediaItem
import com.example.tubesmobdev.data.repository.ListeningRecordRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.util.SongEventBus
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val _activeSession = MutableStateFlow<ListeningSession?>(null)
    private val activeSession: StateFlow<ListeningSession?> get() = _activeSession

    private var currentlyPlayed: Boolean = false

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    @Inject
    lateinit var listeningRecordRepository: ListeningRecordRepository

    @Inject
    lateinit var playerPreferences: PlayerPreferences

    @Inject
    lateinit var songRepo: SongRepository

    private val customCommandLike = SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY)
    private val customCommandShuffle = SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY)
    private val customCommandRepeat = SessionCommand(ACTION_TOGGLE_REPEAT, Bundle.EMPTY)
    private val customCommandSongQueue = SessionCommand(SONG_QUEUE, Bundle.EMPTY)
    private val customCommandDismiss = SessionCommand(ACTION_DISMISS, Bundle.EMPTY)


    companion object {
        const val ACTION_TOGGLE_SHUFFLE = "com.example.tubesmobdev.SHUFFLE"
        const val ACTION_TOGGLE_REPEAT = "com.example.tubesmobdev.REPEAT"
        const val ACTION_TOGGLE_LIKE = "com.example.tubesmobdev.LIKE"
        const val SONG_QUEUE = "com.example.tubesmobdev.SONG_QUEUE"
        const val ACTION_DISMISS = "com.example.tubesmobdev.DISMISS"

    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private inner class CustomCallback : MediaSession.Callback {
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.d("Restore", "playback resume")

            mediaSession.player.play()
            val mediaItems = buildList {
                val count = mediaSession.player.mediaItemCount
                for (i in 0 until count) {
                    add(mediaSession.player.getMediaItemAt(i))
                }
            }

            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(
                    mediaItems,
                    mediaSession.player.currentMediaItemIndex,
                    mediaSession.player.currentPosition
                )
            )
        }


        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.d("eventbaru", "play pause ${customCommand.commandCode}")

             val player = mediaSession?.player
             if (customCommand.customAction == SONG_QUEUE) {
                val json = args.getString("queue")
                val type = object : TypeToken<List<Song>>() {}.type
                _currentQueue.value = Gson().fromJson(json, type)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            val currentIndex = player?.currentMediaItemIndex ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_UNKNOWN))
            val song = _currentQueue.value.getOrNull(currentIndex) ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_UNKNOWN))
            if (customCommand.customAction == ACTION_TOGGLE_LIKE) {
                val newLiked = !song.isLiked
                emitToggleLike(song.id, newLiked)
                val updatedQueue = _currentQueue.value.toMutableList()
                val newSong = song.copy(isLiked = newLiked)
                updatedQueue[currentIndex] = newSong
                _currentQueue.value = updatedQueue
                updateCustomButton(newSong)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            } else if (customCommand.customAction == ACTION_TOGGLE_SHUFFLE) {
                val current = player.shuffleModeEnabled
                val toggled = !current
                player.shuffleModeEnabled = toggled
                emitToggleShuffle(toggled)
                updateCustomButton(song)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            } else if (customCommand.customAction == ACTION_TOGGLE_REPEAT) {
                player.repeatMode = when (player.repeatMode) {
                    REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                    REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                    else -> REPEAT_MODE_OFF
                }
                emitToggleRepeat(player.repeatMode)

                updateCustomButton(song)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            } else if (customCommand.customAction == ACTION_DISMISS) {
                stopListeningSession()
                savePlayerState()
                player.stop()
                stopSelf()
                val exitIntent = Intent(applicationContext, MainActivity::class.java).apply {
                    putExtra("EXIT_AND_REMOVE", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                applicationContext.startActivity(exitIntent)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ConnectionResult {
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(customCommandShuffle)
                        .add(customCommandRepeat)
                        .add(customCommandLike)
                        .add(customCommandSongQueue)
                        .add(customCommandDismiss)
                        .build()
                )
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("Restore", "create")

        val player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                val index = player.currentMediaItemIndex
                val snapshot = _currentQueue.value.toMutableList()
                val oldSong = snapshot.getOrNull(index) ?: return
                Log.d("SongPlayedService", oldSong.toString())


                serviceScope.launch {
                    val newSong = if (oldSong.isOnline) {
                        songRepo.findSongByServerId(oldSong.serverId!!)
                    } else {
                        songRepo.findSongById(oldSong.id)
                    }

                    if (newSong != null) {
                        if (index in snapshot.indices) {
                            snapshot[index] = newSong
                            _currentQueue.value = snapshot
                            player.replaceMediaItem(index, newSong.toMediaItem())
                            clearListeningSession()
                            emitSongChange(newSong)
                            updateCustomButton(newSong)
                        }
                    } else {
                        if (index in snapshot.indices) {
                            player.removeMediaItem(index)
                            snapshot.removeAt(index)
                            _currentQueue.value = snapshot

                            if (player.hasNextMediaItem()) {
                                player.seekToNext()
                            } else {
                                player.seekToPrevious()
                            }
                        }
                    }
                }
            }


            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val index = player.currentMediaItemIndex
                val song = _currentQueue.value.getOrNull(index)
                if (song != null) {
                    if (isPlaying) {
                        Log.d("Change123", "started ${song}")
                        emitSongStarted(song)
                    } else {
                        Log.d("Change123", "paused ${song}")
                        emitSongPaused(song)
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                val index = player.currentMediaItemIndex
                val song = _currentQueue.value.getOrNull(index) ?: return
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    emitSongSeeked(song, oldPosition.positionMs, newPosition.positionMs)
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        putExtra("NAVIGATE_TO_FULL_PLAYER", true)
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setCallback(CustomCallback())
            .build()

        serviceScope.launch {
            restoreUnfinishedListeningSession()
        }

    }

    private fun updateCustomButton(song: Song) {
        val buttons = mutableListOf<CommandButton>()
//        val player = mediaSession?.player

//        val shuffleBtn = CommandButton.Builder()
//            .setDisplayName("Shuffle")
//            .setSessionCommand(customCommandShuffle)
//            .setIconResId(
//                if (player?.shuffleModeEnabled == true)
//                    R.drawable.ic_shuffle
//                else
//                    R.drawable.ic_shuffle_off
//            )
//            .build()

        val closeBtn = CommandButton.Builder()
            .setDisplayName("Close")
            .setSessionCommand(customCommandDismiss)
            .setIconResId(R.drawable.ic_close)
            .build()

        val likeBtn = CommandButton.Builder()
            .setDisplayName("Like")
            .setSessionCommand(customCommandLike)
            .setIconResId(
                if (song.isLiked) R.drawable.ic_liked else R.drawable.ic_like
            )
            .build()


        //            add(
//                CommandButton.Builder()
//                    .setDisplayName("Repeat")
//                    .setSessionCommand(customCommandRepeat)
//                    .setIconResId(
//                        when (player?.repeatMode) {
//                            REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
//                            REPEAT_MODE_ALL -> R.drawable.ic_repeat
//                            else -> R.drawable.ic_repeat_off
//                        }
//                    )
//                    .build()
//            )

//        buttons.add(shuffleBtn)
        buttons.add(closeBtn)
        buttons.add(likeBtn)
        Log.d("PlayerViewModel", "updated button")

        mediaSession?.setCustomLayout(buttons)
    }


    private fun emitSongChange(song: Song) {
        serviceScope.launch {
            currentlyPlayed = true
            SongEventBus.emitSong(song)
            Log.d("MusicService", "Emitted song change via EventBus: ${song.title}")
        }
    }

    private fun emitSongStarted(song: Song) {
        serviceScope.launch {
            currentlyPlayed = true
            startListeningSession(song)
            SongEventBus.emitSongStarted(song)
            Log.d("MusicService", "Emitted song started via EventBus")
        }
    }

    private fun emitSongPaused(song: Song) {
        serviceScope.launch {
            stopListeningSession()
            SongEventBus.emitSongPaused(song)
            Log.d("MusicService", "Emitted song paused via EventBus")
        }
    }

    private fun emitSongSeeked(song: Song, from: Long, to: Long) {
        serviceScope.launch {
            SongEventBus.emitSongSeeked(song, from, to)
            Log.d("MusicService", "Emitted song seeked via EventBus")
        }
    }

    private fun emitToggleLike(songId: Int, isLiked: Boolean) {
        serviceScope.launch {
            SongEventBus.emitLike(songId, isLiked)
            Log.d("MusicService", "Emitted song like via EventBus: $songId ")
        }
    }

    private fun emitToggleRepeat(repeatMode: Int) {
        serviceScope.launch {
            SongEventBus.emitRepeatToggled(repeatMode)
            Log.d("MusicService", "Emitted song like via EventBus ")
        }
    }
    private fun emitToggleShuffle(isShuffle: Boolean) {
        serviceScope.launch {
            SongEventBus.emitShuffleToggled(isShuffle)
            Log.d("MusicService", "Emitted song shuffle via EventBus")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("Restore", "Session ${mediaSession.toString()}")

        return mediaSession
    }

    override fun onDestroy() {
        Log.d("Restore", "Destroyed")
        stopListeningSession()
        clearListeningSession()
        mediaSession?.run {
            player.stop()
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("Restore", "App removed from recent apps")

        serviceScope.launch {
            try {
                stopListeningSession()
                if (_currentQueue.value.isNotEmpty()) {
                    playerPreferences.saveLastQueue(_currentQueue.value)
                }
                val index = mediaSession?.player?.currentMediaItemIndex ?: -1
                if (index in _currentQueue.value.indices) {
                    val currentSong = _currentQueue.value[index]
                    playerPreferences.saveLastPlayedSong(currentSong)
                    val position = mediaSession?.player?.currentPosition ?: 0
                    playerPreferences.saveLastPosition(position)
                }
            } catch (e: Exception) {
                Log.w("Restore", "Gagal menyimpan state terakhir", e)
            } finally {
                mediaSession?.run {
                    player.stop()
                }
                stopSelf()
            }
        }
    }

    private fun savePlayerState() {
        serviceScope.launch {
            try {
                if (_currentQueue.value.isNotEmpty()) {
                    playerPreferences.saveLastQueue(_currentQueue.value)
                }

                val index = mediaSession?.player?.currentMediaItemIndex ?: -1
                if (index in _currentQueue.value.indices) {
                    val currentSong = _currentQueue.value[index]
                    playerPreferences.saveLastPlayedSong(currentSong)
                    val position = mediaSession?.player?.currentPosition ?: 0
                    playerPreferences.saveLastPosition(position)
                }
            } catch (e: Exception) {
                Log.w("MusicService", "Gagal menyimpan state terakhir", e)
            }
        }
    }

    private fun startListeningSession(song: Song) {
        val existing = _activeSession.value
        currentlyPlayed = true

        Log.d("debug", "startListeningSession: $existing")

        if (existing != null && existing.songId == song.id) {
            serviceScope.launch {
                val now = System.currentTimeMillis()
                _activeSession.value = existing.copy(lastKnownTimestamp = now)
                playerPreferences.saveListeningSession(_activeSession.value!!)
            }
            return
        }

        val now = System.currentTimeMillis()
        val sessionId = if (existing != null && existing.songId == song.id && song.id != 0) {
            existing.sessionId
        } else {
            "${song.id}-${now}"
        }

        Log.d("debug", "Creating session: title=${song.title}, artist=${song.artist}, id=${song.id}, sessionId=$sessionId")

        val newSession = ListeningSession(
            songId = song.id,
            title = song.title ?: "Unknown Title",
            artist = song.artist ?: "Unknown Artist",
            sessionId = sessionId,
            startTimestamp = existing?.startTimestamp ?: now,
            lastKnownTimestamp = now,
            coverUrl = song.coverUrl
        )

        _activeSession.value = newSession

        serviceScope.launch {
            try {
                playerPreferences.saveListeningSession(newSession)
            } catch (e: Exception) {
                Log.e("MusicService", "Failed to save session", e)
            }
        }
    }

    private fun stopListeningSession() {
        val session = _activeSession.value ?: return

        Log.d("debug", "stopListeningSession: $session")

        val now = System.currentTimeMillis()
        val duration = (now - session.lastKnownTimestamp).coerceAtLeast(0L)

        if (duration < 5000) {
            Log.d("MusicService", "Durasi terlalu pendek, tidak disimpan")
            return
        }

        serviceScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val existing = listeningRecordRepository.getRecordBySessionId(session.sessionId)
            if (existing != null) {
                val updated = existing.copy(durationListened = existing.durationListened + duration)
                listeningRecordRepository.updateRecord(updated)
            } else {
                val record = ListeningRecord(
                    sessionId = session.sessionId,
                    songId = session.songId,
                    title = session.title,
                    artist = session.artist,
                    date = today,
                    durationListened = duration,
                    coverUrl = session.coverUrl,
                    creatorId = null
                )
                listeningRecordRepository.insertRecord(record)
            }

            val updatedSession = session.copy(lastKnownTimestamp = now)
            _activeSession.value = updatedSession
            playerPreferences.saveListeningSession(updatedSession)
        }
    }

    private fun clearListeningSession() {
        Log.d("debug", "clearListeningSession")
        _activeSession.value = null
        serviceScope.launch {
            playerPreferences.clearListeningSession()
        }
    }

    private suspend fun restoreUnfinishedListeningSession() {
        val session = playerPreferences.getListeningSession()
        if (session != null) {
            val now = System.currentTimeMillis()
            val duration = (now - session.lastKnownTimestamp).coerceAtLeast(0L)

            if (duration >= 5000) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val existing = listeningRecordRepository.getRecordBySessionId(session.sessionId)

                if (existing != null) {
                    val updated = existing.copy(durationListened = existing.durationListened + duration)
                    listeningRecordRepository.updateRecord(updated)
                } else {
                    val record = ListeningRecord(
                        sessionId = session.sessionId,
                        songId = session.songId,
                        title = session.title,
                        artist = session.artist,
                        date = today,
                        durationListened = duration,
                        coverUrl = session.coverUrl,
                        creatorId = null
                    )
                    listeningRecordRepository.insertRecord(record)
                }
            }

            _activeSession.value = null
            playerPreferences.clearListeningSession()
        }
    }
}