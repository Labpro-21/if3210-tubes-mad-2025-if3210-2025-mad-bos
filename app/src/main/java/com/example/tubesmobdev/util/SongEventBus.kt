package com.example.tubesmobdev.util

import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow


object SongEventBus {
    private val _events = MutableSharedFlow<Song>(replay = 1)
    val events: SharedFlow<Song> = _events.asSharedFlow()

    suspend fun emitSong(song: Song) {

        _events.emit(song)
    }
}