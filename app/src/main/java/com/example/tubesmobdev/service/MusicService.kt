package com.example.tubesmobdev.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerNotificationManager
import com.example.tubesmobdev.MainActivity
import com.example.tubesmobdev.R
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.notification.CustomNotificationHelper
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
//@AndroidEntryPoint
class MusicService : MediaSessionService() {
//    @Inject
//    lateinit var songRepository: SongRepository

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var notificationManager: PlayerNotificationManager? = null

    private var currentQueue: List<Song> = emptyList()
    private var currentIndex: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this).build()

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
                return player.mediaMetadata.title ?: "Unknown Title"
            }

            override fun createCurrentContentIntent(player: androidx.media3.common.Player): PendingIntent? {
                return PendingIntent.getActivity(
                    this@MusicService,
                    0,
                    Intent(this@MusicService, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? {
                return player.mediaMetadata.artist ?: "Unknown Artist"
            }

            override fun getCurrentLargeIcon(
                player: androidx.media3.common.Player,
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
            }
//            ACTION_PLAY -> {
//                val queueJson = intent.getStringExtra(EXTRA_QUEUE)
//                val index = intent.getIntExtra(EXTRA_INDEX, 0)
//                val shuffle = intent.getBooleanExtra(EXTRA_SHUFFLE, false)
//                val repeat = intent.getStringExtra(EXTRA_REPEAT) ?: "NONE"
//
//                val queueType = object : TypeToken<List<Song>>() {}.type
//                val queue: List<Song> = Gson().fromJson(queueJson, queueType)
//                val mediaItems = queue.map {
//                    MediaItem.Builder()
//                        .setUri(it.filePath)
//                        .setMediaMetadata(
//                            MediaMetadata.Builder()
//                                .setTitle(it.title)
//                                .setArtist(it.artist)
//                                .setArtworkUri(Uri.parse(it.coverUrl))
//                                .build()
//                        )
//                        .build()
//                }
//
//                currentQueue = queue
//                currentIndex = index
//
//                player?.setMediaItems(mediaItems, index, 0)
//                player?.shuffleModeEnabled = shuffle
//                player?.repeatMode = when (repeat) {
//                    "REPEAT_ONE" -> REPEAT_MODE_ONE
//                    "REPEAT_ALL" -> REPEAT_MODE_ALL
//                    else -> REPEAT_MODE_OFF
//                }
//                player?.prepare()
//                player?.play()
//
//                // ✅ Tambahkan bagian custom notification di sini
//                val currentSong = queue.getOrNull(index)
////                val coverBitmap = try {
////                    val uri = Uri.parse(currentSong?.coverUrl)
////                    val inputStream = contentResolver.openInputStream(uri)
////                    BitmapFactory.decodeStream(inputStream).also { inputStream?.close() }
////                } catch (e: Exception) {
////                    null
////                }
//
//                val notification = CustomNotificationHelper.createNotification(
//                    context = this,
//                    title = currentSong?.title ?: "Unknown",
//                    artist = currentSong?.artist ?: "",
//                    cover = null,
//                    isPlaying = player?.isPlaying ?: false
//                )
//
//                val manager = getSystemService(NotificationManager::class.java)
//                stopForeground(true)
//                manager.cancelAll()
//                manager.notify(CustomNotificationHelper.NOTIFICATION_ID, notification)
//                startForeground(CustomNotificationHelper.NOTIFICATION_ID, notification)
//            }
            ACTION_STOP -> {
                stopSelf()
            }
//            CustomNotificationHelper.ACTION_PLAY_PAUSE -> {
//                if (player?.isPlaying == true) player?.pause() else player?.play()
//            }
//            CustomNotificationHelper.ACTION_NEXT -> {
//                player?.seekToNext()
//            }
//            CustomNotificationHelper.ACTION_PREV -> {
//                player?.seekToPrevious()
//            }
//            CustomNotificationHelper.ACTION_LIKE -> {
//                val index = player?.currentMediaItemIndex
//                val song = index?.let { currentQueue.getOrNull(it) }
//
//                if (index == null || song == null) {
//                    Log.w("MusicService", "Like toggle failed: no song at index")
//                } else {
//                    val newLikedState = !song.isLiked
//                    val updatedSong = song.copy(isLiked = newLikedState)
//
//                    currentQueue = currentQueue.toMutableList().apply {
//                        set(index, updatedSong)
//                    }
//
//                    Log.d("MusicService", "Toggling like: ${updatedSong.title} → $newLikedState")
//
//                    CoroutineScope(Dispatchers.IO).launch {
//                        songRepository.updateLikedStatus(song.id, newLikedState)
//                    }
//                }
//            }
//
//            CustomNotificationHelper.ACTION_SHUFFLE -> {
//                val newShuffle = !(player?.shuffleModeEnabled ?: false)
//                player?.shuffleModeEnabled = newShuffle
//                Log.d("MusicService", "Shuffle toggled: $newShuffle")
//            }
//            CustomNotificationHelper.ACTION_REPEAT -> {
//                val currentRepeat = player?.repeatMode ?: REPEAT_MODE_OFF
//                val newRepeat = when (currentRepeat) {
//                    REPEAT_MODE_OFF -> REPEAT_MODE_ALL
//                    REPEAT_MODE_ALL -> REPEAT_MODE_ONE
//                    else -> REPEAT_MODE_OFF
//                }
//                player?.repeatMode = newRepeat
//                Log.d("MusicService", "Repeat mode changed: $newRepeat")
//            }
        }
        return START_STICKY
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
    }
}
