package com.example.tubesmobdev.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
@Composable
fun SeekSlider(
    progress: Float,
    durationMillis: Int,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = progress) {
        if (!isDragging) {
            sliderPosition = progress
        }
    }
    val currentSeconds = ((sliderPosition * durationMillis) / 1000).roundToInt()
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "$minutes:${secs.toString().padStart(2, '0')}"
    }

    Column(modifier = modifier) {
        if (isDragging) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(currentSeconds),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
                isDragging = true
            },
            onValueChangeFinished = {
                onSeekFinished(sliderPosition)
                isDragging = false
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Gray
            )
        )
    }
}

