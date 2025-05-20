package com.example.tubesmobdev.ui.components
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.tubesmobdev.service.generateQRCodeUrl

@Composable
fun ShareOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
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
    onQRClicked: () -> Unit,
    isSheetOpen: Boolean,
    sheetState: SheetState,
    onCloseSheet: () -> Unit,
    onSongUpdate: (Song) -> Unit,
    onShareClicked: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    customTopBar: @Composable (() -> Unit) = {},
    isCompact: Boolean
) {
    var showAudioDialog by rememberSaveable { mutableStateOf(false) }
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShareSheetOpen by remember { mutableStateOf(false) }
    val dominantColor = rememberDominantColor(song.coverUrl ?: "").copy(alpha = 0.9f)
    val bgPainter = rememberAsyncImagePainter(song.coverUrl ?: "")
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



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dominantColor)

    ) {
        if (!isCompact) {
            Image(
                painter       = bgPainter,
                contentScale  = ContentScale.Crop,
                contentDescription = null,
                modifier      = Modifier
                    .matchParentSize()
            )
            Box(modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.4f)))
        } else {
            Box(modifier = Modifier
                .matchParentSize()
                .background(dominantColor))
        }
        if (!isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    customTopBar()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SeekSlider(
                        progress        = progress,
                        durationMillis  = totalDurationMillis,
                        onSeekFinished  = onSeekTo,
                        modifier        = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSkipPrevious) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint               = Color.White
                            )
                        }
                        IconButton(
                            onClick   = onTogglePlayPause,
                            modifier  = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector        = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint               = Color.Black,
                                modifier           = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = onSkipNext) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint               = Color.White,
                                modifier           = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                        .size(450.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = if (song.coverUrl.isNullOrEmpty()) {
                            painterResource(id = R.drawable.music)
                        } else {
                            rememberAsyncImagePainter(song.coverUrl)
                        },
                        contentDescription = song.title + "-image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                            .clip(RoundedCornerShape(12.dp))
                            .size(200.dp)
                            .aspectRatio(1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp, end = 16.dp)
                        ) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showAudioDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Speaker,
                                    contentDescription = "Ganti Output Audio",
                                    tint = Color.White
                                )
                            }
                        }
                        if (song.isOnline) {
                            IconButton(onClick = { isShareSheetOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Song",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(onClick = onAddClicked) {
                                Icon(
                                    imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like/Unlike",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }else{
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
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    IconButton(onClick = onTogglePlayPause,    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        )) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = onSkipNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = onCycleRepeat) {
                        val repeatIcon = when (repeatMode) {
                            RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }

                        val repeatIconColor = when (repeatMode) {
                            RepeatMode.NONE -> Color.Gray
                            RepeatMode.REPEAT_ALL -> Color.White
                            RepeatMode.REPEAT_ONE -> Color(0xFF4CAF50)
                        }

                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = "Repeat",
                            tint = repeatIconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

            }
        }
        if (isSheetOpen) {
            AddSongDrawer(
                sheetState = sheetState,
                onDismissRequest = onCloseSheet,
                onClose = onCloseSheet,
                onResult = { result: Result<Unit> ->
                    if (result.isSuccess) {
                        onShowSnackbar("Lagu berhasil disimpan")
                    } else {
                        onShowSnackbar("Gagal menyimpan lagu")
                    }
                    onCloseSheet()
                },
                songToEdit = song,
                onSongUpdate = onSongUpdate
            )
        }
        if (showAudioDialog) {
            AudioRoutingDialog(onDismiss = { showAudioDialog = false })
        }
        if (isShareSheetOpen) {
            val qrBitmap = remember(song) { generateQRCodeUrl(song) }
            val qrPainter = remember(qrBitmap) { BitmapPainter(qrBitmap.asImageBitmap()) }
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            ModalBottomSheet(
                onDismissRequest = { isShareSheetOpen = false },
                sheetState = shareSheetState,
                containerColor = Color.Black,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))


                    Image(
                        painter = qrPainter,
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = song.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(24.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ShareOption(
                            icon = Icons.Default.ContentCopy,
                            label = "Copy Link",
                            onClick = {
                                val uri = "purrytify://song/${song.serverId}"
                                clipboardManager.setText(AnnotatedString(uri))
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                                isShareSheetOpen = false
                            }
                        )

                        ShareOption(
                            icon = Icons.Default.Link,
                            label = "Share Link",
                            onClick = {
                                isShareSheetOpen = false
                                onShareClicked()
                            }
                        )

                        ShareOption(
                            icon = Icons.Default.QrCode,
                            label = "Share QR",
                            onClick = {
                                isShareSheetOpen = false
                                onQRClicked()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
