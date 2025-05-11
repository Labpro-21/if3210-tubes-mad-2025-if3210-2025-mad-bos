package com.example.tubesmobdev.data.remote.response

import com.example.tubesmobdev.data.model.Song

data class OnlineSong(
    val id: Int,
    val title: String,
    val artist: String,
    val artwork: String,
    val url: String,
    val duration: String,
    val country: String,
    val rank: Int,
    val createdAt: String,
    val updatedAt: String
)

fun OnlineSong.toLocalSong(): Song {
    val song = Song(
        title = this.title,
        artist = this.artist,
        filePath = this.url,
        coverUrl = this.artwork,
        duration = parseDuration(this.duration),
        createdAt = System.currentTimeMillis()
    )
    song.isOnline = true
    return song
}

fun parseDuration(durationStr: String): Long {
    val parts = durationStr.split(":")
    return parts[0].toLong() * 60_000 + parts[1].toLong() * 1000
}