package com.example.tubesmobdev.ui.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default,
    startDelayMillis: Int = 1000,
    endDelayMillis: Int = 2000,
    velocity: Float = 30f
) {
    val scrollState = rememberScrollState()
    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }

    LaunchedEffect (textWidth, containerWidth) {
        Log.d("Effect", "$textWidth $containerWidth")
        if (textWidth <= containerWidth * 0.8) return@LaunchedEffect

        while (true) {
            scrollState.scrollTo(0)
            delay(startDelayMillis.toLong())

            val distance = textWidth - (containerWidth * 0.8)
            val duration = (distance / velocity * 1000).toInt()

            scrollState.animateScrollTo(distance.toInt(), animationSpec = tween(duration))
            delay(endDelayMillis.toLong())

            scrollState.scrollTo(0)
            delay(endDelayMillis.toLong())
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .horizontalScroll(scrollState)
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            }
    ) {
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            style = textStyle,
            onTextLayout = { result ->
                textWidth = result.size.width
            },
            modifier = Modifier
                .padding(end = 60.dp)
                .then(Modifier)
        )
    }
}
