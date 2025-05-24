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
import com.example.tubesmobdev.data.local.preferences.PlayerPreferences
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.util.SongEventBus
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@UnstableApi
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    private var currentQueue: List<Song> = emptyList()
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private lateinit var playerPreferences: PlayerPreferences

    private val customCommandLike = SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY)
    private val customCommandShuffle = SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY)
    private val customCommandRepeat = SessionCommand(ACTION_TOGGLE_REPEAT, Bundle.EMPTY)
    private val customCommandSongQueue = SessionCommand(SONG_QUEUE, Bundle.EMPTY)


    companion object {
        const val ACTION_PLAY = "com.example.tubesmobdev.PLAY"
        const val ACTION_STOP = "com.example.tubesmobdev.STOP"

        const val EXTRA_QUEUE = "EXTRA_QUEUE"
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val EXTRA_SHUFFLE = "EXTRA_SHUFFLE"
        const val EXTRA_REPEAT = "EXTRA_REPEAT"

        const val ACTION_TOGGLE_SHUFFLE = "com.example.tubesmobdev.SHUFFLE"
        const val ACTION_TOGGLE_REPEAT = "com.example.tubesmobdev.REPEAT"
        const val ACTION_TOGGLE_LIKE = "com.example.tubesmobdev.LIKE"
        const val SONG_QUEUE = "com.example.tubesmobdev.SONG_QUEUE"

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
             val player = mediaSession?.player
             if (customCommand.customAction == SONG_QUEUE) {
                val json = args.getString("queue")
                val type = object : TypeToken<List<Song>>() {}.type
                currentQueue = Gson().fromJson(json, type)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            val currentIndex = player?.currentMediaItemIndex ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_UNKNOWN))
            val song = currentQueue.getOrNull(currentIndex) ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_UNKNOWN))
            if (customCommand.customAction == ACTION_TOGGLE_LIKE) {
                val newLiked = !song.isLiked
                emitToggleLike(song.id, newLiked)
                val updatedQueue = currentQueue.toMutableList()
                val newSong = song.copy(isLiked = newLiked)
                updatedQueue[currentIndex] = newSong
                currentQueue = updatedQueue
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
                        .build()
                )
                .build()
        }


    }


    override fun onCreate() {
        super.onCreate()

        Log.d("Restore", "create")

        playerPreferences = PlayerPreferences(applicationContext)

        val player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val index = player.currentMediaItemIndex
                if (index >= 0 && index < currentQueue.size) {
                    val song = currentQueue[index]
                    emitSongChange(song)
                    updateCustomButton(song)

                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {

                    val index = player.currentMediaItemIndex
                    val nextIndex = when {
                        player.repeatMode == REPEAT_MODE_ONE -> index
                        index < currentQueue.size - 1 -> index + 1
                        player.repeatMode == REPEAT_MODE_ALL -> 0
                        else -> -1
                    }

                    if (nextIndex >= 0 && nextIndex < currentQueue.size) {
                        val nextSong = currentQueue[nextIndex]

                        emitSongChange(nextSong)
                    }
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

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!isConnectedToInternet()) {
            Log.w("MusicService", "No internet connection!")
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.action) {
            ACTION_PLAY -> {
                val queueJson = intent.getStringExtra(EXTRA_QUEUE)
                val index = intent.getIntExtra(EXTRA_INDEX, 0)
                val shuffle = intent.getBooleanExtra(EXTRA_SHUFFLE, false)
                val repeat = intent.getStringExtra(EXTRA_REPEAT) ?: "NONE"


                val queueType = object : TypeToken<List<Song>>() {}.type
                val queue: List<Song> = Gson().fromJson(queueJson, queueType)
                val mediaItems = queue.map {
                    MediaItem.Builder()
                        .setMediaId(it.title)
                        .setUri(it.filePath)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(it.title)
                                .setArtist(it.artist)
                                .setArtworkUri(Uri.parse(it.coverUrl))
                                .build()
                        )
                        .build()
                }


                currentQueue = queue
                val player = mediaSession?.player

                player?.setMediaItems(mediaItems, index, 0)
                player?.shuffleModeEnabled = shuffle
                player?.repeatMode = when (repeat) {
                    "REPEAT_ONE" -> REPEAT_MODE_ONE
                    "REPEAT_ALL" -> REPEAT_MODE_ALL
                    else -> REPEAT_MODE_OFF
                }
                player?.prepare()
                player?.play()

                if (queue.isNotEmpty() && index >= 0 && index < queue.size) {
                    Log.d("PlayerViewModel", "actionplay: $queue, $index", )

                    emitSongChange(queue[index])
                }
            }

            ACTION_STOP -> {
                stopSelf()
            }

        }
        return START_STICKY
    }

    private fun updateCustomButton(song: Song) {
        val buttons = mutableListOf<CommandButton>()
        val player = mediaSession?.player

        val shuffleBtn = CommandButton.Builder()
            .setDisplayName("Shuffle")
            .setSessionCommand(customCommandShuffle)
            .setIconResId(
                if (player?.shuffleModeEnabled == true)
                    R.drawable.ic_shuffle
                else
                    R.drawable.ic_shuffle_off
            )
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

        buttons.add(shuffleBtn)

        buttons.add(likeBtn)
        Log.d("PlayerViewModel", "updated button")

        mediaSession?.setCustomLayout(buttons)
    }


    private fun emitSongChange(song: Song) {
        serviceScope.launch {
            SongEventBus.emitSong(song)
            Log.d("MusicService", "Emitted song change via EventBus: ${song.title}")
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
        if (rootIntent != null) {
            Log.d("Restore", rootIntent.action.toString())
        }

        serviceScope.launch {
            try {
                if (currentQueue.isNotEmpty()) {
                    playerPreferences.saveLastQueue(currentQueue)
                }

                val index = mediaSession?.player?.currentMediaItemIndex ?: -1
                if (index in currentQueue.indices) {
                    val currentSong = currentQueue[index]
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
}