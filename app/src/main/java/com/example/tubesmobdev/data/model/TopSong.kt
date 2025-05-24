package com.example.tubesmobdev.data.model

data class TopSong(
    val songId: Int,
    val monthYear: String,   // misal "2024-05"
    val title    : String,
    val coverUrl : String?,
    val artist: String?,
    val playCount: Int
)