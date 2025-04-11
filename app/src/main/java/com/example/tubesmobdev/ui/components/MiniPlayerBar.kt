package com.example.tubesmobdev.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.util.rememberDominantColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.example.tubesmobdev.R
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlayPause: () -> Unit,
    onAddClicked: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val dominantColor = rememberDominantColor(song.coverUrl ?: "").copy(alpha = 0.9f)
    val swipeThreshold = 200f
    var offsetX by remember { mutableStateOf(0f) }
    val swipeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val maxOffsetForAlpha = 300f
    val alpha = 1f - (abs(swipeOffset.value) / maxOffsetForAlpha).coerceIn(0f, 0.7f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(dominantColor)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                        coroutineScope.launch {
                            swipeOffset.snapTo(offsetX)
                        }
                    },
                    onDragEnd = {
                        when {
                            offsetX > swipeThreshold -> {
                                onSwipeRight()
                            }
                            offsetX < -swipeThreshold -> {
                                onSwipeLeft()
                            }
                        }
                        offsetX = 0f
                        coroutineScope.launch {
                            swipeOffset.animateTo(0f, animationSpec = tween(300))
                        }
                    }
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = if (song.coverUrl.isNullOrEmpty()) {
                    painterResource(id = R.drawable.music)
                } else {
                    rememberAsyncImagePainter(song.coverUrl)
                },
                contentDescription = song.title,
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .zIndex(2f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .alpha(alpha)
                    .zIndex(1f)
            ) {
                AnimatedContent(
                    targetState = song,
                    transitionSpec = {
                        if (targetState.id > initialState.id) {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) with
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
                        } else {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) with
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
                        }
                    },
                    label = "SongTextTransition"
                ) { currentSong ->
                    Column {
                        Text(
                            text = currentSong.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Text(
                            text = currentSong.artist,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = onAddClicked) {
                Icon(
                    imageVector = if (song.isLiked) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (song.isLiked) "Liked" else "Add",
                    tint = Color.White
                )
            }

            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.DarkGray,
        )
    }
}
