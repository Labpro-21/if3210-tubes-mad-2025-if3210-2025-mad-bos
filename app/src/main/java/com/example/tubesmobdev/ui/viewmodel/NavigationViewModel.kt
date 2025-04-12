package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubesmobdev.data.local.preferences.IServicePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NavigationViewModel @Inject constructor(
    authPreferences: IAuthPreferences,
    private val servicePreferences: IServicePreferences
) : ViewModel() {
//    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
//    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()



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
//    init {
//        checkLoginStatus()
//    }
//
//    private fun checkLoginStatus() {
//        viewModelScope.launch {
//            val loggedIn = authPreferences.isLoggedIn()
//            _isLoggedIn.value = loggedIn
//        }
//    }
}