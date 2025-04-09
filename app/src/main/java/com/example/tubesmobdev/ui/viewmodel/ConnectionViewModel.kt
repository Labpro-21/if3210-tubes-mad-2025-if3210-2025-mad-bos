package com.example.tubesmobdev.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.service.ConnectivityObserver
import com.example.tubesmobdev.service.ConnectivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ConnectionViewModel(
    connectivityObserver: ConnectivityObserver,
): ViewModel() {
    val connectivityStatus: StateFlow<ConnectivityStatus> =
        connectivityObserver.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectivityStatus.Available
        )
}