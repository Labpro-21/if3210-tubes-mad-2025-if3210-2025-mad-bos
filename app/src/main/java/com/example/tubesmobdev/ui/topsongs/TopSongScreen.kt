package com.example.tubesmobdev.ui.topsongs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.R
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.remote.response.toLocalSong
import com.example.tubesmobdev.ui.components.SongListItem
import com.example.tubesmobdev.ui.viewmodel.OnlineSongViewModel

@Composable
fun TopSongsScreen(
    chartCode: String,
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: OnlineSongViewModel = hiltViewModel()
) {
    val onlineSongs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(chartCode) {
        viewModel.fetchSongs(chartCode)
    }

    val songs = onlineSongs.sortedBy { it.rank }

    val backgroundGradient = Brush.verticalGradient(
        colors = if (chartCode == "global")
            listOf(Color(0xFF1d7d75), Color(0xFF1d4c6a), Color(0xFF1e3264), Color(0xFF121212))
        else
            listOf(Color(0xFFf16975), Color(0xFFec1e32), Color(0xFF121212)),
    )

    val cardGradient = Brush.verticalGradient(
        colors = if (chartCode == "global")
            listOf(Color(0xFF1d7d75), Color(0xFF1d4c6a), Color(0xFF1e3264))
        else
            listOf(Color(0xFFf16975), Color(0xFFec1e32))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()

        ) {
            item {
                Column(modifier = Modifier.background(brush = backgroundGradient)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(230.dp)
                                .aspectRatio(1f)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    clip = false
                                )
                                .background(
                                    brush = cardGradient,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (chartCode == "global") "Top 50" else "Top 10",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(1.dp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = chartCode.uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Your daily update of the most played tracks right now – ${chartCode.uppercase()}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            if (songs.isNotEmpty()) onSongClick(songs[0].toLocalSong(), songs.map{ it.toLocalSong() })
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF1DB954),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                    }

                    if (error != null) {
                        Text(
                            text = "Error: $error",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            itemsIndexed(songs) { _, song ->
                SongListItem(
                    number = song.rank,
                    song = song,
                    onClick = { onSongClick(song.toLocalSong(), listOf(song.toLocalSong())) }
                )
            }
        }
    }
}
