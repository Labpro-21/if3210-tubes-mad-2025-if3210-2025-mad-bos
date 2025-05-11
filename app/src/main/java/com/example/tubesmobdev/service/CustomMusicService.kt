package com.example.tubesmobdev.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import com.example.tubesmobdev.MainActivity
import com.example.tubesmobdev.R
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.util.RepeatMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CustomMusicService : Service() {

    private var currentQueue: List<Song> = emptyList()
    private var currentIndex: Int = 0
    private var isPlaying: Boolean = true
    private var isShuffle: Boolean = false
    private var repeatMode: RepeatMode = RepeatMode.NONE

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val queueJson = intent.getStringExtra(EXTRA_QUEUE)
                val index = intent.getIntExtra(EXTRA_INDEX, 0)
                val shuffle = intent.getBooleanExtra(EXTRA_SHUFFLE, false)
                val repeat = intent.getStringExtra(EXTRA_REPEAT) ?: "NONE"

                val queueType = object : TypeToken<List<Song>>() {}.type
                val queue: List<Song> = Gson().fromJson(queueJson, queueType)

                currentQueue = queue
                currentIndex = index
                isShuffle = shuffle
                repeatMode = RepeatMode.valueOf(repeat)
                isPlaying = true

                updateNotification()
            }
            ACTION_PLAY_PAUSE -> {
                isPlaying = !isPlaying
                updateNotification()
            }
            ACTION_NEXT -> {
                if (currentQueue.isNotEmpty()) {
                    currentIndex = (currentIndex + 1) % currentQueue.size
                    isPlaying = true
                    updateNotification()
                }
            }
            ACTION_PREV -> {
                if (currentQueue.isNotEmpty()) {
                    currentIndex = if (currentIndex - 1 < 0) currentQueue.lastIndex else currentIndex - 1
                    isPlaying = true
                    updateNotification()
                }
            }
            ACTION_LIKE -> {
                val song = currentQueue.getOrNull(currentIndex) ?: return START_STICKY
                currentQueue = currentQueue.toMutableList().apply {
                    set(currentIndex, song.copy(isLiked = !song.isLiked))
                }
                updateNotification()
            }
            ACTION_SHUFFLE -> {
                isShuffle = !isShuffle
                updateNotification()
            }
            ACTION_REPEAT -> {
                repeatMode = when (repeatMode) {
                    RepeatMode.NONE -> RepeatMode.REPEAT_ALL
                    RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                    RepeatMode.REPEAT_ONE -> RepeatMode.NONE
                }
                updateNotification()
            }
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun updateNotification() {
        val song = currentQueue.getOrNull(currentIndex) ?: return

        val cover = try {
            val uri = Uri.parse(song.coverUrl)
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }

        val remoteViews = RemoteViews(packageName, R.layout.notification_custom_player).apply {
            setTextViewText(R.id.titleTextView, song.title)
            setTextViewText(R.id.artistTextView, song.artist)
            setImageViewBitmap(R.id.coverImageView, cover)

            setImageViewResource(R.id.playPauseButton, if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            setImageViewResource(R.id.likeButton, if (song.isLiked) R.drawable.ic_liked else R.drawable.ic_like)
            setImageViewResource(R.id.shuffleButton, if (isShuffle) R.drawable.ic_shuffle else R.drawable.ic_shuffle)
            setImageViewResource(R.id.repeatButton, when (repeatMode) {
                RepeatMode.REPEAT_ONE -> R.drawable.ic_repeat_one
                RepeatMode.REPEAT_ALL -> R.drawable.ic_repeat
                RepeatMode.NONE -> R.drawable.ic_repeat
            })

            setOnClickPendingIntent(R.id.playPauseButton, pendingIntent(ACTION_PLAY_PAUSE))
            setOnClickPendingIntent(R.id.prevButton, pendingIntent(ACTION_PREV))
            setOnClickPendingIntent(R.id.nextButton, pendingIntent(ACTION_NEXT))
            setOnClickPendingIntent(R.id.likeButton, pendingIntent(ACTION_LIKE))
            setOnClickPendingIntent(R.id.shuffleButton, pendingIntent(ACTION_SHUFFLE))
            setOnClickPendingIntent(R.id.repeatButton, pendingIntent(ACTION_REPEAT))
        }

        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = android.app.Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setCustomContentView(remoteViews)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun pendingIntent(action: String): PendingIntent {
        return PendingIntent.getService(
            this,
            action.hashCode(),
            Intent(this, CustomMusicService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "music_channel_custom"
        private const val NOTIFICATION_ID = 888

        const val ACTION_PLAY = "custom.ACTION_PLAY"
        const val ACTION_STOP = "custom.ACTION_STOP"
        const val ACTION_PLAY_PAUSE = "custom.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "custom.ACTION_NEXT"
        const val ACTION_PREV = "custom.ACTION_PREV"
        const val ACTION_LIKE = "custom.ACTION_LIKE"
        const val ACTION_SHUFFLE = "custom.ACTION_SHUFFLE"
        const val ACTION_REPEAT = "custom.ACTION_REPEAT"

        const val EXTRA_QUEUE = "custom.EXTRA_QUEUE"
        const val EXTRA_INDEX = "custom.EXTRA_INDEX"
        const val EXTRA_SHUFFLE = "custom.EXTRA_SHUFFLE"
        const val EXTRA_REPEAT = "custom.EXTRA_REPEAT"
    }
}
