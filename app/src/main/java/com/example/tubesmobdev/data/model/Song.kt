package com.example.tubesmobdev.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
) {
    @Ignore
    var isOnline: Boolean = false

    fun copy(
        id: Int = this.id,
        title: String = this.title,
        artist: String = this.artist,
        filePath: String = this.filePath,
        coverUrl: String? = this.coverUrl,
        duration: Long = this.duration,
        isLiked: Boolean = this.isLiked,
        lastPlayed: Long? = this.lastPlayed,
        createdAt: Long? = this.createdAt,
        creatorId: Long? = this.creatorId,
        isOnline: Boolean = this.isOnline,
        isDownloaded: Boolean = this.isDownloaded
    ): Song {
        val song = Song(
            id, title, artist, filePath, coverUrl,
            duration, isLiked, lastPlayed, createdAt, creatorId
        )
        song.isOnline = isOnline
        return song
    }
}