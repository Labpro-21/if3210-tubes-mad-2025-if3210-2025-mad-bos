package com.example.tubesmobdev.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
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
import com.example.tubesmobdev.data.model.StreakEntry

/**
 * Data per bulan untuk ditampilkan di Sound Capsule.
 */
data class SoundCapsuleData(
    val month: String,
    val minutesListened: Long,
    val topArtist: TopArtist?,
    val topSong: TopSong?,
    val streakEntry: StreakEntry?,
    val streakSong: Song?,
    val streakRange: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundCapsuleSection(
    capsules: List<SoundCapsuleData>,
    onShareStreak: (SoundCapsuleData) -> Unit
) {
    if (capsules.isEmpty()) {
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
        capsules.forEach { data ->
            // Reuse UI style from second code block per bulan
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                // Title
                Text(
                    text = "Your Sound Capsule",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.month,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onShareStreak(data) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Capsule",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Minutes listened card
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
                                Text(
                                    text = "Time listened",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${data.minutesListened} minutes",
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
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Artist & Song Row
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
                                Text(data.topArtist!!.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(16.dp))
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Artist icon",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Details artist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
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
                                Text(data.topSong!!.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(16.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(data.topSong.coverUrl),
                                    contentDescription = "Song image",
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
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Streak Card
                if (data.streakSong != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            Image(
                                painter = rememberAsyncImagePainter(data.streakSong.coverUrl),
                                contentDescription = "Streak image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Text(
                                    text = "You had a ${data.streakEntry!!.days}-day streak",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "You played ${data.streakSong.artist} day after day. You were on fire",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = data.streakRange,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
