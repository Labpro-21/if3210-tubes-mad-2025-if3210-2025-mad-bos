package com.example.tubesmobdev.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tubesmobdev.data.model.SoundCapsuleStreakShareData
import com.example.tubesmobdev.util.rememberDominantColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import com.example.tubesmobdev.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SoundCapsuleShareStreakContent(
    data: SoundCapsuleStreakShareData,
    modifier: Modifier = Modifier
) {
    val coverUrl = data.streakSong?.coverUrl.orEmpty()
    val artist = data.streakSong?.artist.orEmpty()
    val title = data.streakSong?.title.orEmpty()
    val dominantColor = rememberDominantColor(coverUrl).copy(alpha = 0.9f)
    val context = LocalContext.current

    val streakDays = data.streakSong?.let { data.streakRange }?.split("-")?.let {
        val days = it.firstOrNull()?.takeLast(2)?.toIntOrNull()
        val end = it.lastOrNull()?.takeLast(2)?.toIntOrNull()
        if (days != null && end != null) (end - days + 1).coerceAtLeast(1) else 0
    } ?: 0

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(dominantColor)
            .padding(24.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF3B3B3B)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My $streakDays-day streak",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(12.dp))

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .allowHardware(false) // ⬅️ FIX agar tidak error saat drawToBitmap
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = artist,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(8.dp))
                }
                Surface(
                    color = Color(0xFF1F1F1F),
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.logo_app),
                                contentDescription = "App Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Purritify",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                            )
                        }

                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                        )
                    }
                }
            }
        }
    }
}