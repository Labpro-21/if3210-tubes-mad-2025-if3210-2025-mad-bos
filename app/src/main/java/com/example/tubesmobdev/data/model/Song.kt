package com.example.tubesmobdev.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
) {

}