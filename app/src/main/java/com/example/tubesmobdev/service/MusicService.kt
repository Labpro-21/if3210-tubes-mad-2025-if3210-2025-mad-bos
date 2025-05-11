package com.example.tubesmobdev.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.tubesmobdev.MainActivity
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

    companion object {
        private const val NOTIFICATION_ID = 69
        private const val CHANNEL_ID = "music_channel"

        const val ACTION_PLAY = "com.example.tubesmobdev.PLAY"
        const val ACTION_STOP = "com.example.tubesmobdev.STOP"
        const val EXTRA_URI = "EXTRA_URI"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_ARTIST = "EXTRA_ARTIST"

        const val EXTRA_QUEUE = "EXTRA_QUEUE"
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val EXTRA_SHUFFLE = "EXTRA_SHUFFLE"
        const val EXTRA_REPEAT = "EXTRA_REPEAT"

        const val ACTION_SONG_CHANGED = "com.example.tubesmobdev.SONG_CHANGED"
        const val EXTRA_SONG = "EXTRA_SONG"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this).build()

        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val index = player?.currentMediaItemIndex ?: return
                if (index >= 0 && index < currentQueue.size) {
                    val song = currentQueue[index]
                    Log.d("PlayerViewModel", "onmediatrans: $song", )
                    emitSongChange(song)
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
            .setCallback(object : MediaSession.Callback {
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
            })
            .build()

        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return player.mediaMetadata.title ?: "Unknown Title"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(this@MusicService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("NAVIGATE_TO_FULL_PLAYER", true)
                }

                Log.d("Intent3", intent.toString())

                return PendingIntent.getActivity(
                    this@MusicService,
                    0,
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return player.mediaMetadata.artist ?: "Unknown Artist"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val song = currentQueue.getOrNull(player.currentMediaItemIndex) ?: return null
                val coverPath = song.coverUrl ?: return null

                return try {
                    val uri = Uri.parse(coverPath)
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    bitmap
                } catch (e: Exception) {
                    null
                }
            }
        }).setNotificationListener(object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: android.app.Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    startForeground(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                stopSelf()
            }
        }).build()

        notificationManager?.apply {
            setPlayer(player)
            setMediaSessionToken(mediaSession!!.sessionCompatToken)
            setUseNextAction(true)
            setUsePreviousAction(true)
            setUsePlayPauseActions(true)
            setUseStopAction(true)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY -> {
                val queueJson = intent.getStringExtra(EXTRA_QUEUE)
                val index = intent.getIntExtra(EXTRA_INDEX, 0)
                val shuffle = intent.getBooleanExtra(EXTRA_SHUFFLE, false)
                val repeat = intent.getStringExtra(EXTRA_REPEAT) ?: "NONE"

                Log.d("MusicService2", queueJson.toString())

                val queueType = object : TypeToken<List<Song>>() {}.type
                val queue: List<Song> = Gson().fromJson(queueJson, queueType)
                val mediaItems = queue.map {
                    MediaItem.Builder()
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

                Log.d("MusicService", queue.toString())

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

    private fun emitSongChange(song: Song) {
        serviceScope.launch {
            SongEventBus.emitSong(song)
            Log.d("MusicService", "Emitted song change via EventBus: ${song.title}")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        mediaSession?.release()
        player?.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}