package com.example.tubesmobdev.data.model

data class StreakEntry(
    val monthYear: String,
    val songId   : Int,
    val title    : String,
    val startDate: String,
    val endDate  : String,
    val days     : String,
)

data class MonthlyStreakSong(
    val monthYear: String,
    val song: Song
)