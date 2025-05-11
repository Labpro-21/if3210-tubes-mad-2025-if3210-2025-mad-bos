package com.example.tubesmobdev.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.local.preferences.IServicePreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NavigationViewModel @Inject constructor(
    authPreferences: IAuthPreferences,
    private val servicePreferences: IServicePreferences
) : ViewModel() {

    private val _navigateToFullPlayer = MutableSharedFlow<Unit>(extraBufferCapacity = 1,replay = 1)
    val navigateToFullPlayer = _navigateToFullPlayer

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized
    val isLoggedIn: StateFlow<Boolean> = authPreferences.isLoggedInFlow
        .onEach { _isInitialized.value = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setShouldRestartService(value: Boolean) {
        viewModelScope.launch {
            servicePreferences.setShouldRestartService(value)
        }
    }

    fun triggerNavigateToFullPlayer() {
        Log.d("MainLayout", "triggerNavigateToFullPlayer")
        val res = _navigateToFullPlayer.tryEmit(Unit)
        Log.d("MainLayout", "tryemit: $res")
    }

}