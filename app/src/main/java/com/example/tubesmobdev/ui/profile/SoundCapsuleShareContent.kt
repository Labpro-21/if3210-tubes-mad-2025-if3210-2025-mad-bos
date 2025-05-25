package com.example.tubesmobdev.ui.profile

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tubesmobdev.data.model.SoundCapsuleShareData
import com.example.tubesmobdev.util.formatMonthYear
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SoundCapsuleShareContent(data: SoundCapsuleShareData, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val imageUrl = data.topArtist.firstOrNull()?.coverUrl
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .allowHardware(false)
            .build()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1C1C1E),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Purritify", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "My ${formatMonthYear(data.month)} Sound Capsule",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Top artists", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        data.topArtist.take(5).forEachIndexed { i, artist ->
                            Text(
                                text = "${i + 1} ${artist.artist}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Top songs", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        data.topSong.take(5).forEachIndexed { i, song ->
                            Text(
                                text = "${i + 1} ${song.title}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Time listened", color = Color.Gray)
                Text(
                    text = "${data.minutesListened} minutes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF41D18D),
                        fontSize = 24.sp
                    )

                )
            }
        }
    }
}