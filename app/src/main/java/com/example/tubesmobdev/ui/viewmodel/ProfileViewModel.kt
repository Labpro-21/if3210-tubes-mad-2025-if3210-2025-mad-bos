package com.example.tubesmobdev.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.local.preferences.ServicePreferences
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.MonthlyStreakSong
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.model.StreakEntry
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
import javax.inject.Inject



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: IAuthRepository,
    private val songRepository: SongRepository,
    private val listeningRecordRepository: ListeningRecordRepository,
    private val playerManager: PlayerManager,
    private val servicePreferences: ServicePreferences
) : ViewModel() {

    // --- PROFILE & LOADING STATE ---
    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- SONG COUNTS ---
    private val _allSongsCount = MutableStateFlow(0)
    val allSongsCount: StateFlow<Int> = _allSongsCount.asStateFlow()

    private val _likedSongsCount = MutableStateFlow(0)
    val likedSongsCount: StateFlow<Int> = _likedSongsCount.asStateFlow()

    private val _listenedSongsCount = MutableStateFlow(0)
    val listenedSongsCount: StateFlow<Int> = _listenedSongsCount.asStateFlow()

    // --- LISTENING STATS ---
    private val _totalListeningMinutes = MutableStateFlow(0L)
    val totalListeningMinutes: StateFlow<Long> = _totalListeningMinutes.asStateFlow()

    private val _allRecords = MutableStateFlow<List<ListeningRecord>>(emptyList())
    val allRecords: StateFlow<List<ListeningRecord>> = _allRecords.asStateFlow()

    private val _topArtists = MutableStateFlow<List<TopArtist>>(emptyList())
    val topArtists: StateFlow<List<TopArtist>> = _topArtists.asStateFlow()

    private val _topSongs = MutableStateFlow<List<TopSong>>(emptyList())
    val topSongs: StateFlow<List<TopSong>> = _topSongs.asStateFlow()

    private val _monthlyStreaks = MutableStateFlow<List<StreakEntry>>(emptyList())
    val monthlyStreaks: StateFlow<List<StreakEntry>> = _monthlyStreaks.asStateFlow()

    private val _monthlyStreakSongs = MutableStateFlow<List<MonthlyStreakSong>>(emptyList())
    val monthlyStreakSongs: StateFlow<List<MonthlyStreakSong>> = _monthlyStreakSongs.asStateFlow()

    init {
        fetchProfile()
        fetchSongCounts()
        observeListeningStats()
        observeAllRecords()
    }

    private fun fetchSongCounts() {
        viewModelScope.launch {
            songRepository.getAllSongsCount()
                .collect { _allSongsCount.value = it }
        }
        viewModelScope.launch {
            songRepository.getLikedSongsCount()
                .collect { _likedSongsCount.value = it }
        }
        viewModelScope.launch {
            songRepository.getPlayedSongsCount()
                .collect { _listenedSongsCount.value = it }
        }
    }

    private fun observeListeningStats() {
        // total listening time (menit)
        viewModelScope.launch {
            listeningRecordRepository.getTotalListeningTime()
                .collect { millis ->
                    _totalListeningMinutes.value = millis / 60000
                }
        }
        // daftar top artist (tahun lalu)
        viewModelScope.launch {
            listeningRecordRepository.getTopArtistLastYear()
                .collect { _topArtists.value = it }
        }
        // daftar top song (tahun lalu)
        viewModelScope.launch {
            listeningRecordRepository.getTopSongLastYear()
                .collect { _topSongs.value = it }
        }
        // daftar streak per bulan
        viewModelScope.launch {
            listeningRecordRepository.getMonthlyTopStreak()
                .collect { entries ->
                    _monthlyStreaks.value = entries
                    val userId = _profile.value?.id?.toLong() ?: return@collect
                    // Build monthYear→Song list
                    val list = entries.mapNotNull { entry ->
                        val song = withContext(Dispatchers.IO) {
                            songRepository.getSongById(entry.songId, userId)
                        } ?: return@mapNotNull null
                        Log.d("debug", "observeListeningStats: "+ song)
                        MonthlyStreakSong(
                            monthYear = entry.monthYear,
                            song = song
                        )
                    }
                    _monthlyStreakSongs.value = list
                }
        }
    }

    private fun observeAllRecords() {
        viewModelScope.launch {
            listeningRecordRepository.getAllRecords()
                .collect { _allRecords.value = it }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            profileRepository.getProfile().fold(
                onSuccess = { resp ->
                    _profile.value = resp
                },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Unknown error"
                    Log.d("ProfileViewModel", err.message.toString())
                }
            )

            _isLoading.value = false
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
            val result = withContext(Dispatchers.IO) {
                profileRepository.updateProfilePhoto(context, uri)
            }
            result.onFailure {
                _errorMessage.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun updateLocation(location: String) {
        _profile.value = _profile.value?.copy(location = location)
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) {
                profileRepository.updateLocation(location)
            }
            result.onFailure {
                _errorMessage.value = it.message
            }
            _isLoading.value = false
        }
    }
}