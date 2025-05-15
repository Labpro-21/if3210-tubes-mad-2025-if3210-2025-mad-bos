package com.example.tubesmobdev.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.local.preferences.ServicePreferences
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.model.TopArtist
import com.example.tubesmobdev.data.model.TopSong
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import com.example.tubesmobdev.data.repository.IAuthRepository
import com.example.tubesmobdev.data.repository.ListeningRecordRepository
import com.example.tubesmobdev.data.repository.ProfileRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: IAuthRepository,
    private val songRepository: SongRepository,
    private val listeningRecordRepository: ListeningRecordRepository,
    private val playerManager: PlayerManager,
    private val servicePreferences: ServicePreferences,
) : ViewModel() {

    val totalListeningMinutes = MutableStateFlow(0L)
    val topArtist = MutableStateFlow<TopArtist?>(null)
    val topSong = MutableStateFlow<TopSong?>(null)

    // Sound capsule streak data
    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()
    private val _streakSong = MutableStateFlow<Song?>(null)
    val streakSong: StateFlow<Song?> = _streakSong.asStateFlow()
    private val _streakRange = MutableStateFlow("")
    val streakRange: StateFlow<String> = _streakRange.asStateFlow()

    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _allSongsCount = MutableStateFlow(0)
    val allSongsCount: StateFlow<Int> = _allSongsCount.asStateFlow()

    private val _likedSongsCount = MutableStateFlow(0)
    val likedSongsCount: StateFlow<Int> = _likedSongsCount.asStateFlow()

    private val _listenedSongsCount = MutableStateFlow(0)
    val listenedSongsCount: StateFlow<Int> = _listenedSongsCount.asStateFlow()

    init {
        fetchProfile()
        fetchSongCounts()
    }

    private fun fetchSongCounts() {
        viewModelScope.launch {
            songRepository.getAllSongsCount()
                .collect { _allSongsCount.value = it }
            songRepository.getLikedSongsCount()
                .collect { _likedSongsCount.value = it }
            songRepository.getPlayedSongsCount()
                .collect { _listenedSongsCount.value = it }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = profileRepository.getProfile()
            result.fold(
                onSuccess = { resp ->
                    _profile.value = resp
                    setupSoundCapsule(resp.id.toLong())
                },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Unknown error"
                    Log.d("ProfileViewModel", err.message.toString())
                }
            )
            _isLoading.value = false

            // common flows
            viewModelScope.launch {
                listeningRecordRepository.getTotalListeningTime()
                    .collect { totalListeningMinutes.value = (it ?: 0L) / 60000 }
            }

            viewModelScope.launch {
                listeningRecordRepository.getTopArtist()
                    .collect { topArtist.value = it }
            }
            viewModelScope.launch {
                listeningRecordRepository.getTopSong()
                    .collect { topSong.value = it }
            }
        }
    }


    private fun setupSoundCapsule(userId: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val entries = listeningRecordRepository.getRecordsForStreakAnalysis()
            // group by title and map to sorted distinct dates
            val formatter = DateTimeFormatter.ISO_DATE
            data class StreakInfo(
                val title: String,
                val songId: Int,
                val dates: List<LocalDate>
            )
            val infoList = entries.groupBy { it.title }
                .map { (title, list) ->
                    val dates = list.map { LocalDate.parse(it.date, formatter) }
                        .distinct().sorted()
                    StreakInfo(title, list.first().songId, dates)
                }
            // compute max consecutive streak
            var bestTitle = ""
            var bestId = 0
            var bestStart = LocalDate.now()
            var bestEnd = LocalDate.now()
            var maxStreak = 0

            infoList.forEach { info ->
                var current = 1
                var start = info.dates.firstOrNull() ?: return@forEach
                info.dates.zipWithNext().forEach { (prev, next) ->
                    if (next == prev.plusDays(1)) {
                        current++;
                    } else {
                        current = 1; start = next
                    }
                    if (current > maxStreak) {
                        maxStreak = current
                        bestTitle = info.title
                        bestId = info.songId
                        bestStart = start
                        bestEnd = next
                    }
                }
                // handle single-day only
                if (info.dates.isNotEmpty() && maxStreak == 0) {
                    maxStreak = 1; bestTitle = info.title; bestId = info.songId
                    bestStart = info.dates.first(); bestEnd = info.dates.first()
                }
            }

            _streakDays.value = maxStreak
            _streakRange.value = if (maxStreak > 1) {
                "${bestStart.format(formatter)}–${bestEnd.format(formatter)}"
            } else "${bestStart.format(formatter)}"

            // fetch coverUrl
            val song = songRepository.getSongById(bestId, userId)
            _streakSong.value = song
        }
    }

    fun logout() {
        viewModelScope.launch {
            servicePreferences.setShouldRestartService(false)
            authRepository.logout()
            playerManager.clearWithCallback()
        }
    }

    fun updateProfilePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) { profileRepository.updateProfilePhoto(context, uri) }
            result.onSuccess { fetchProfile() }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}
