package com.example.tubesmobdev.ui.components
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.util.RepeatMode
import com.example.tubesmobdev.util.rememberDominantColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.tubesmobdev.R
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
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
    onSeekTo: (Float) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    isSheetOpen: Boolean,
    sheetState: SheetState,
    onCloseSheet: () -> Unit,
    onSongUpdate: (Song) -> Unit,
    onShowSnackbar: (String) -> Unit,
    customTopBar: @Composable (() -> Unit) = {},
    isCompact: Boolean
) {
    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val dominantColor = rememberDominantColor(song.coverUrl ?: "").copy(alpha = 0.9f)
    val totalDurationMillis = song.duration.toInt()
    val totalDurationSeconds = (totalDurationMillis / 1000L).toInt()
    val currentSeconds = (progress * totalDurationSeconds).roundToInt()
    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }
    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    val swipeOffset = remember { Animatable(0f) }
    val swipeThreshold = 100f

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            onShowSnackbar(it)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isCompact){
                customTopBar()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }.size(450.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = if (song.coverUrl.isNullOrEmpty()) {
                        painterResource(id = R.drawable.music)
                    } else {
                        rememberAsyncImagePainter(song.coverUrl)
                    },
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                        .clip(RoundedCornerShape(12.dp))
                        .size(200.dp)
                        .aspectRatio(1f)
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
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = onSkipNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White,modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = onCycleRepeat) {
                    val repeatIcon = when (repeatMode) {
                        RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)                    )
                }
                IconButton(onClick = onAddClicked) {
                    Icon(
                        imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like/Unlike",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        if (isSheetOpen) {
            AddSongDrawer(
                sheetState = sheetState,
                onDismissRequest = onCloseSheet,
                onClose = onCloseSheet,
                onResult = { result: Result<Unit> ->
                    snackbarMessage = if (result.isSuccess) {
                        "Lagu berhasil disimpan"
                    } else {
                        "Gagal menyimpan lagu"
                    }
                    onCloseSheet()
                },
                songToEdit = song, // LANGSUNG current song aja
                onSongUpdate = onSongUpdate
            )
        }
    }
}