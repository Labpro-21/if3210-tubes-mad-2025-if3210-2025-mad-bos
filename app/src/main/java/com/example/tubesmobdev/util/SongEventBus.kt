package com.example.tubesmobdev.util

import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SongEvent {
    data class SongChanged(val song: Song) : SongEvent()
    data class SongLiked(val songId: Int, val isLiked: Boolean) : SongEvent()
    data class ShuffleToggled(val isShuffle: Boolean) : SongEvent()
    data class RepeatToggled(val repeatMode: Int) : SongEvent()
//    data object StopApp : SongEvent()
}

object SongEventBus {
    private val _events = MutableSharedFlow<SongEvent>(replay = 1)
    val events: SharedFlow<SongEvent> = _events.asSharedFlow()

    suspend fun emitSong(song: Song) {
        _events.emit(SongEvent.SongChanged(song))
    }

    suspend fun emitLike(songId: Int, isLiked: Boolean) {
        _events.emit(SongEvent.SongLiked(songId, isLiked))
    }

    suspend fun emitShuffleToggled(isShuffle: Boolean) {
        _events.emit(SongEvent.ShuffleToggled(isShuffle))
    }

    suspend fun emitRepeatToggled(repeatMode: Int) {
        _events.emit(SongEvent.RepeatToggled(repeatMode))
    }

//    suspend fun emitStopApp() {
//        _events.emit(SongEvent.StopApp)
//    }
}
