package com.example.tubesmobdev.data.model

import kotlinx.coroutines.flow.Flow

data class SoundCapsuleData(
    val month: String,
    val minutesListened: Long,
    val topArtist: TopArtist?,
    val topSong: TopSong?,
    val streakEntry: StreakEntry?,
    val streakSong: Song?,
    val streakRange: String
)
data class SoundCapsuleShareData(
    val month: String,
    val minutesListened: Long,
    val topArtist: List<TopArtist>,
    val topSong: List<TopSong>
)
data class SoundCapsuleStreakShareData(
    val month: String,
    val streakSong: Song?,
    val streakRange: String
)