package com.example.tubesmobdev.ui.topsongs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.ui.components.SongListItem
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.OnlineSongViewModel
import com.example.tubesmobdev.util.ProfileUtil

@Composable
fun RecomendationSongScreen(
    chartCode: String,
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: OnlineSongViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit,
    updateSongAfterDownload: () -> Unit,
    currentSong: Song?
) {
    val context = LocalContext.current
    val onlineSongs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val connectivityStatus by connectionViewModel.connectivityStatus.collectAsState()

    val downloadingSongs by viewModel.downloadingSongs.collectAsState()

    val downloadedSongIds by viewModel.downloadedSongIds.collectAsState()


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
                                    text = "Song Recomendations",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = titleFontSize,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (connectivityStatus == ConnectivityStatus.Available) {
                        Text(
                            text = "Fresh picks just for you. Dive into your daily mix of the most played tracks.",
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
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    if (songs.isNotEmpty()) {
                                        viewModel.convertToLocalSongs(songs) { localSongs ->
                                            onSongClick(localSongs.first(), localSongs)
                                        }
                                    }
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
                            viewModel.convertToLocalSongs(listOf(song)) { localSongs ->
                                onSongClick(localSongs.first(), localSongs)
                            }
                        },
                        onDownloadClick = {
                            viewModel.downloadAndInsertSong(context, song) { result -> val message = if (result.isSuccess) {
                                "Lagu berhasil diunduh"
                            } else {
                                result.exceptionOrNull()?.message ?: "Gagal mengunduh lagu"
                            }
                                onShowSnackbar(message)
                                if (currentSong != null) {
                                    if (currentSong.serverId == song.id){
                                        updateSongAfterDownload()
                                    }
                                }
                            }

                        },
                        isDownloading = downloadingSongs[song.id] == true,
                        isDownloaded = downloadedSongIds.contains(song.id)
                    )
                }
            }
        }
    }
}