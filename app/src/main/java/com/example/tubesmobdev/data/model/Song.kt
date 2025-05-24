package com.example.tubesmobdev.data.model

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tubesmobdev.data.remote.response.OnlineSong
import com.example.tubesmobdev.data.remote.response.parseDuration

@Entity(tableName = "songs",
    indices = [Index(value = ["serverId"], unique = true)])
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverId: Int? = null,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverUrl: String?,
    val duration: Long,
    val isLiked: Boolean = false,
    val lastPlayed: Long? = null,
    val createdAt: Long? = null,
    var creatorId: Long? = null,
    val isDownloaded: Boolean = false,
    var isOnline: Boolean = false,
)

fun Song.toOnlineSong(): OnlineSong {
    val onlineSong = OnlineSong(
        id = this.serverId ?: 0,
        title = this.title,
        artist = this.artist,
        url = this.filePath,
        artwork = this.coverUrl ?: "",
        duration = formatDuration(this.duration),
        rank = 0,
        createdAt = "",
        updatedAt = "",
        country = ""
    )
    return onlineSong
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(this.title)
        .setUri(this.filePath)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(this.title)
                .setArtist(this.artist)
                .setArtworkUri(Uri.parse(this.coverUrl))
                .build()
        )
        .build()
}

fun formatDuration(durationMillis: Long): String {
    val minutes = durationMillis / 60_000
    val seconds = (durationMillis % 60_000) / 1000
    return "%02d:%02d".format(minutes, seconds)
}