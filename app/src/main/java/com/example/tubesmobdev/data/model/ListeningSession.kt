package com.example.tubesmobdev.data.model

data class ListeningSession(
    val sessionId: String,
    val songId: Int,
    val title: String,
    val artist: String,
    val creatorId: Long?,
    val coverUrl: String?,
    val startTimestamp: Long,
    val lastKnownTimestamp: Long = startTimestamp,
    val accumulatedDuration: Long = 0L
)