package com.example.tubesmobdev.ui.viewmodel

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tubesmobdev.domain.model.AudioOutputDevice
import com.example.tubesmobdev.manager.AudioRoutingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class AudioRoutingViewModel @Inject constructor(
    private val audioRoutingManager: AudioRoutingManager
) : ViewModel() {

    private val _devices = mutableStateListOf<AudioOutputDevice>()
    val devices: List<AudioOutputDevice> = _devices

    private val _selectedDevice = mutableStateOf<AudioOutputDevice?>(null)
    val selectedDevice: State<AudioOutputDevice?> = _selectedDevice

    private var isObserving = false

    init {
        loadDevices()
        observeRouteChanges()
    }

    fun loadDevices() {
        _devices.clear()
        _devices.addAll(audioRoutingManager.getAvailableRoutes())
        _selectedDevice.value = _devices.find { it.isConnected }
    }

    fun selectDevice(device: AudioOutputDevice) {
        audioRoutingManager.selectDevice(device.id)
        loadDevices()
    }

    private fun observeRouteChanges() {
        if (isObserving) return
        isObserving = true

        audioRoutingManager.observeRouteChanges {
            loadDevices()
        }
    }
}