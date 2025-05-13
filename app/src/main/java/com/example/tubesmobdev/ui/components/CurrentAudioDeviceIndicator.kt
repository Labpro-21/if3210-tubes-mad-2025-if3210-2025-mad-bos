package com.example.tubesmobdev.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.ui.viewmodel.AudioRoutingViewModel

@Composable
fun CurrentAudioDeviceIndicator(viewModel: AudioRoutingViewModel = hiltViewModel()) {
    viewModel.selectedDevice.value?.let { device ->
        Text(
            text = "Output: ${device.name}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}