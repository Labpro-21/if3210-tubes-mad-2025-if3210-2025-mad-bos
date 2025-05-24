package com.example.tubesmobdev.data.model

data class TopArtist(
    val monthYear: String,   // format "YYYY-MM"
    val artist   : String,
    val coverUrl : String?,
    val playCount: Int
)
