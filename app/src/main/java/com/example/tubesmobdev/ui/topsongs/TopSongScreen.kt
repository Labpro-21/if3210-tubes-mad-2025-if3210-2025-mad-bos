package com.example.tubesmobdev.ui.topsongs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.R
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.remote.response.toLocalSong
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.ui.components.SongListItem
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.OnlineSongViewModel
import com.example.tubesmobdev.util.ProfileUtil

@Composable
fun TopSongsScreen(
    chartCode: String,
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: OnlineSongViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current
    val onlineSongs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val currentDownloadTitle by viewModel.currentDownloadTitle.collectAsState()

    val connectivityStatus by connectionViewModel.connectivityStatus.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val cardWidth = (screenWidth * 0.6f).coerceAtMost(230.dp)
    val verticalPadding = (screenHeight * 0.06f).coerceAtMost(50.dp).coerceAtLeast(20.dp)

    val titleFontSize = when {
        screenWidth < 320.dp -> 20.sp
        screenWidth > 600.dp -> 32.sp
        else -> (20 + (screenWidth.value - 320) / (600 - 320) * (32 - 20)).sp
    }

    val bodyFontSize = when {
        screenWidth < 320.dp -> 12.sp
        screenWidth > 600.dp -> 16.sp
        else -> (12 + (screenWidth.value - 320) / (600 - 320) * (16 - 12)).sp
    }


    LaunchedEffect(chartCode) {
        viewModel.fetchSongs(chartCode)
    }

    LaunchedEffect(isDownloading) {
        if (isDownloading) {
            onShowSnackbar("Downloading $currentDownloadTitle")
        }
    }

    val songs = onlineSongs.sortedBy { it.rank }

    val backgroundGradient = Brush.verticalGradient(
        colors = if (connectivityStatus == ConnectivityStatus.Available) {
            if (chartCode == "global")
                listOf(Color(0xFF1d7d75), Color(0xFF1d4c6a), Color(0xFF1e3264), Color(0xFF121212))
            else
                listOf(Color(0xFFf16975), Color(0xFFec1e32), Color(0xFF121212))
        } else {
            listOf(Color(0xFFB0B0B0), Color(0xFF8C8C8C), Color(0xFF121212))
        }
    )

    val cardGradient = Brush.verticalGradient(
        colors = if (connectivityStatus == ConnectivityStatus.Available) {
            if (chartCode == "global")
                listOf(Color(0xFF1d7d75), Color(0xFF1d4c6a), Color(0xFF1e3264))
            else
                listOf(Color(0xFFf16975), Color(0xFFec1e32))
        } else {
            listOf(Color(0xFFB0B0B0), Color(0xFF8C8C8C))
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(modifier = Modifier.background(brush = backgroundGradient)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = verticalPadding),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(cardWidth)
                                .aspectRatio(1f)
                                .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
                                .background(cardGradient, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = if (connectivityStatus == ConnectivityStatus.Available) {
                                        if (chartCode == "global") "Top 50" else "Top 10"
                                    } else {
                                        "No Internet"
                                    },
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = titleFontSize,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                if (connectivityStatus == ConnectivityStatus.Available) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .width(cardWidth * 0.5f)
                                            .height(1.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = if (chartCode == "global") chartCode.uppercase()
                                        else ProfileUtil.getCountryName(chartCode).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = bodyFontSize),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (connectivityStatus == ConnectivityStatus.Available) {
                        Text(
                            text = "Your daily update of the most played tracks right now – ${chartCode.uppercase()}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.logo_app),
                                contentDescription = "App Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                            Text(
                                text = "By Purrity • Apr 2025 • 2h 55min",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDownloading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Downloading",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(4.dp),
                                        color = Color(0xFF1DB954),
                                        trackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            IconButton(
                                onClick = {
                                    if (songs.isNotEmpty()) onSongClick(
                                        songs[0].toLocalSong(),
                                        songs.map { it.toLocalSong() }
                                    )
                                },
                                enabled = connectivityStatus == ConnectivityStatus.Available
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = if (connectivityStatus == ConnectivityStatus.Available) Color(0xFF1DB954) else Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        if (error != null) {
                            Text(
                                text = "Error: $error",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            if (connectivityStatus == ConnectivityStatus.Available) {
                itemsIndexed(songs) { _, song ->
                    SongListItem(
                        number = song.rank,
                        song = song,
                        onClick = {
                            onSongClick(song.toLocalSong(), listOf(song.toLocalSong()))
                        },
                        onDownloadClick = {
                            if (!isDownloading) {
                                viewModel.downloadAndInsertSong(context, song, connectionViewModel.connectivityStatus) { result ->
                                val message = if (result.isSuccess) {
                                        "Lagu berhasil diunduh"
                                    } else {
                                        result.exceptionOrNull()?.message ?: "Gagal mengunduh lagu"
                                    }
                                    onShowSnackbar(message)
                                }
                            } else {
                                onShowSnackbar("Download sedang berjalan. Tunggu hingga selesai.")
                            }
                        },
                        isDownloadDisabled = isDownloading
                    )
                }
            }
        }
    }
}