package com.example.tubesmobdev.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.util.RepeatMode
import com.example.tubesmobdev.util.rememberDominantColor
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlayPause: () -> Unit,
    onAddClicked: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onSeekTo: (Float) -> Unit
) {
    val dominantColor = rememberDominantColor(song.coverUrl ?: "").copy(alpha = 0.9f)
    val totalDurationMillis = song.duration.toInt()
    val totalDurationSeconds = (totalDurationMillis / 1000L).toInt()
    val currentSeconds = (progress * totalDurationSeconds).roundToInt()
    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dominantColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(song.coverUrl),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .size(300.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 20.sp
            )
            Text(
                text = song.artist,
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentSeconds), color = Color.White, fontSize = 12.sp)
                Text(text = formatTime(totalDurationSeconds), color = Color.White, fontSize = 12.sp)
            }
            SeekSlider(
                progress = progress,
                durationMillis = totalDurationMillis,
                onSeekFinished = onSeekTo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) Color.Yellow else Color.White
                    )
                }

                IconButton(onClick = onSkipPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                }

                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                }

                IconButton(onClick = onCycleRepeat) {
                    val repeatIcon = when (repeatMode) {
                        RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onAddClicked) {
                    Icon(
                        imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like/Unlike",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

