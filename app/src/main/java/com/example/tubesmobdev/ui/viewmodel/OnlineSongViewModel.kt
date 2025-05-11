package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.remote.response.OnlineSong
import com.example.tubesmobdev.data.repository.OnlineSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(
    private val repository: OnlineSongRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<OnlineSong>>(emptyList())
    val songs: StateFlow<List<OnlineSong>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchSongs(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = if (code.lowercase() == "global") {
                    repository.getTopGlobalSongs()
                } else {
                    repository.getTopSongsByCountry(code)
                }
                _songs.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
