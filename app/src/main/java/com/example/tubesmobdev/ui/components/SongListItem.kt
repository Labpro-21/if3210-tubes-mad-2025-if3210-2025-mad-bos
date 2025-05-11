package com.example.tubesmobdev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tubesmobdev.data.remote.response.OnlineSong

@Composable
fun SongListItem(
    number: Int,
    song: OnlineSong,
    onClick: () -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = number.toString(),
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            model = song.artwork,
            contentDescription = song.title,
            modifier = Modifier
                .size(60.dp)
                .padding(start = 8.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                )
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp
                ),
                color = Color.Gray
            )
        }
    }
}