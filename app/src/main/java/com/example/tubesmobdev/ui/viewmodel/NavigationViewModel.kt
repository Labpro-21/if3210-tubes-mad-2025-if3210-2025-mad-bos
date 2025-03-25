package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val authPreferences: IAuthPreferences
) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val loggedIn = authPreferences.isLoggedIn()
            _isLoggedIn.value = loggedIn
        }
    }
}