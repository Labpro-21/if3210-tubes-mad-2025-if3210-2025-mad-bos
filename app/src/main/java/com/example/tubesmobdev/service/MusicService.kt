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
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerNotificationManager
import com.example.tubesmobdev.MainActivity
import com.example.tubesmobdev.R
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
    private var player: ExoPlayer? = null
    private var notificationManager: PlayerNotificationManager? = null

    private var currentQueue: List<Song> = emptyList()
    private var currentIndex: Int = 0
    private val serviceScope = CoroutineScope(Dispatchers.Main)


    private val customCommandLike = SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY)
    private val customCommandShuffle = SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY)
    private val customCommandRepeat = SessionCommand(ACTION_TOGGLE_REPEAT, Bundle.EMPTY)


    companion object {
        private const val NOTIFICATION_ID = 69
        private const val CHANNEL_ID = "music_channel"

        const val ACTION_PLAY = "com.example.tubesmobdev.PLAY"
        const val ACTION_STOP = "com.example.tubesmobdev.STOP"
        const val ACTION_TOGGLE_SHUFFLE = "com.example.tubesmobdev.SHUFFLE"
        const val ACTION_TOGGLE_REPEAT = "com.example.tubesmobdev.REPEAT"
        const val ACTION_TOGGLE_LIKE = "com.example.tubesmobdev.LIKE"

        const val EXTRA_QUEUE = "EXTRA_QUEUE"
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val EXTRA_SHUFFLE = "EXTRA_SHUFFLE"
        const val EXTRA_REPEAT = "EXTRA_REPEAT"

    }

    private inner class CustomCallback : MediaSession.Callback {
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
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
                val current = player?.shuffleModeEnabled ?: false
                val toggled = !current
                player?.shuffleModeEnabled = toggled
                emitToggleShuffle(toggled)
                updateCustomButton(song)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            } else if (customCommand.customAction == ACTION_TOGGLE_REPEAT) {
                player?.repeatMode = when (player?.repeatMode) {
                    REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                    REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                    else -> REPEAT_MODE_OFF
                }
                emitToggleRepeat(player?.repeatMode ?: REPEAT_MODE_OFF)

                updateCustomButton(song)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ConnectionResult {
            Log.d("CustomButton", "connect")
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(customCommandShuffle)
                        .add(customCommandRepeat)
                        .add(customCommandLike)
                        .build()
                )
                .build()
        }
    }


    override fun onCreate() {
        super.onCreate()


        player = ExoPlayer.Builder(this).build()

        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val index = player?.currentMediaItemIndex ?: return
                if (index >= 0 && index < currentQueue.size) {
                    val song = currentQueue[index]
                    Log.d("PlayerViewModel", "onmediatrans: $song", )
                    emitSongChange(song)
                    updateCustomButton(song)

                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {

                    val index = player?.currentMediaItemIndex ?: return
                    val nextIndex = when {
                        player?.repeatMode == REPEAT_MODE_ONE -> index
                        index < currentQueue.size - 1 -> index + 1
                        player?.repeatMode == REPEAT_MODE_ALL -> 0
                        else -> -1
                    }

                    if (nextIndex >= 0 && nextIndex < currentQueue.size) {
                        val nextSong = currentQueue[nextIndex]
                        Log.d("PlayerViewModel", "onplaybackstate: $nextSong, $playbackState", )

                        emitSongChange(nextSong)
                    }
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setCallback(CustomCallback())
            .build()



//        notificationManager = PlayerNotificationManager.Builder(
//            this,
//            NOTIFICATION_ID,
//            CHANNEL_ID
//        ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
//            override fun getCurrentContentTitle(player: Player): CharSequence {
//                return player.mediaMetadata.title ?: "Unknown Title"
//            }
//
//            override fun createCurrentContentIntent(player: Player): PendingIntent? {
//                val intent = Intent(this@MusicService, MainActivity::class.java).apply {
//                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    putExtra("NAVIGATE_TO_FULL_PLAYER", true)
//                }
//
//                return PendingIntent.getActivity(
//                    this@MusicService,
//                    0,
//                    intent,
//                    PendingIntent.FLAG_MUTABLE
//                )
//            }
//
//            override fun getCurrentContentText(player: Player): CharSequence? {
//                return player.mediaMetadata.artist ?: "Unknown Artist"
//            }
//
//            override fun getCurrentLargeIcon(
//                player: Player,
//                callback: PlayerNotificationManager.BitmapCallback
//            ): Bitmap? {
//                val song = currentQueue.getOrNull(player.currentMediaItemIndex) ?: return null
//                val coverPath = song.coverUrl ?: return null
//
//                return try {
//                    val uri = Uri.parse(coverPath)
//                    val inputStream = contentResolver.openInputStream(uri)
//                    val bitmap = BitmapFactory.decodeStream(inputStream)
//                    inputStream?.close()
//                    bitmap
//                } catch (e: Exception) {
//                    null
//                }
//            }
//        }).setNotificationListener(object : PlayerNotificationManager.NotificationListener {
//            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//                stopSelf()
//            }
//        }).build()
//
//        notificationManager?.apply {
//            setPlayer(player)
//            setSmallIcon(R.drawable.logo_app)
//            setMediaSessionToken(mediaSession!!.sessionCompatToken)
//            setUseNextAction(true)
//            setUsePreviousAction(true)
//            setUsePlayPauseActions(true)
//            setUseStopAction(true)
//        }




    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
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
                currentIndex = index

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

        val likeBtn = if (!song.isOnline) {
            CommandButton.Builder()
                .setDisplayName("Like")
                .setSessionCommand(customCommandLike)
                .setIconResId(
                    if (song.isLiked) R.drawable.ic_liked else R.drawable.ic_like
                )
                .build()
        } else null

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

        if (likeBtn != null) {
            buttons.add(likeBtn)
        }
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
        return mediaSession
    }

    override fun onDestroy() {
//        notificationManager?.setPlayer(null)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }



}