package com.example.tubesmobdev.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SeekSlider(
    progress: Float,
    durationMillis: Int,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(progress) {
        if (!isDragging) {
            sliderPosition = progress
        }
    }
    val currentSeconds = ((sliderPosition * durationMillis) / 1000).roundToInt()

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }
    val currentTimeText = formatTime(currentSeconds)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = { newValue ->
                    sliderPosition = newValue
                    isDragging = true
                },
                onValueChangeFinished = {
                    isDragging = false
                    onSeekFinished(sliderPosition)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                )
            )
        }
        Box(modifier = Modifier.height(24.dp)) {
            Text(
                text = currentTimeText,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(if (isDragging) 1f else 0f)
            )
        }
    }
}
