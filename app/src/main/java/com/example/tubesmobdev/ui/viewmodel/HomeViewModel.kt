package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SongRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> get() = _songs

    private fun fetchSongs() {
        viewModelScope.launch {
            repository.getAllSongs().collect { _songs.value = it }
        }
    }

    fun logout(navController: NavController) {
        viewModelScope.launch {

            authRepository.logout()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }
}