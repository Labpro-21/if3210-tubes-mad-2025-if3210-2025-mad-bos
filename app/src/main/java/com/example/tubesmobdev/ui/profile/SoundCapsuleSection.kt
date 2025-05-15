package com.example.tubesmobdev.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.model.TopArtist
import com.example.tubesmobdev.data.model.TopSong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundCapsuleSection(
    month: String,
    minutesListened: Long,
    topArtist: TopArtist?,
    topSong: TopSong?,
    streakDays: Int,
    streakSong: Song?,
    streakRange: String,
    onShareStreak: () -> Unit
) {
    // Jika data tidak tersedia
    if (minutesListened == 0L || topArtist == null || topSong == null || streakDays == 0 || streakSong == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp)) {
        Text(
            text = "Your Sound Capsule",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Column() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = month,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onShareStreak) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share Capsule",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Minutes listened
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Time listened",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "$minutesListened minutes",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF41D18D)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
//                            .clickable()
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            // Top artist & top song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("Top artist", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Text(topArtist.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(16.dp))
                            Image(
                                painter = rememberAsyncImagePainter(topArtist.coverUrl),
                                contentDescription = "${topArtist.artist}-image",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details artist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
//                                .clickable { /* onTopArtistClick() */ }
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("Top song", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Text(topSong.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(16.dp))
                            Image(
                                painter = rememberAsyncImagePainter(topSong.coverUrl),
                                contentDescription = "${topSong.title}-image",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details song",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
//                                .clickable { /* onTopSongClick() */ }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Streak card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Image(
                        painter = rememberAsyncImagePainter(streakSong.coverUrl),
                        contentDescription = streakSong.title + "-image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "You had a ${streakDays}-day streak",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "You played ${streakSong.title} by ${streakSong.artist} day after day. You were on fire",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = streakRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

    }
}
