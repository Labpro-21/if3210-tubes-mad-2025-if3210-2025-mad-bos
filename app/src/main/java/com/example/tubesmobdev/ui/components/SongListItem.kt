package com.example.tubesmobdev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tubesmobdev.data.remote.response.OnlineSong

@Composable
fun SongListItem(
    number: Int,
    song: OnlineSong,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    isDownloadDisabled: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val numberWidth = when {
        screenWidth < 360 -> 20.dp
        else -> 24.dp
    }

    val artworkSize = when {
        screenWidth < 360 -> 48.dp
        screenWidth > 600 -> 72.dp
        else -> 60.dp
    }

    val horizontalPadding = when {
        screenWidth < 360 -> 8.dp
        screenWidth > 600 -> 16.dp
        else -> 12.dp
    }

    val titleFontSize = when {
        screenWidth < 360 -> 14.sp
        screenWidth > 600 -> 18.sp
        else -> 16.sp
    }

    val artistFontSize = when {
        screenWidth < 360 -> 12.sp
        screenWidth > 600 -> 16.sp
        else -> 14.sp
    }

    val iconSize = when {
        screenWidth < 360 -> 20.dp
        screenWidth > 600 -> 28.dp
        else -> 24.dp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = horizontalPadding, vertical = horizontalPadding * 0.75f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = number.toString(),
            modifier = Modifier.width(numberWidth),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        AsyncImage(
            model = song.artwork,
            contentDescription = song.title,
            modifier = Modifier
                .size(artworkSize)
                .padding(start = horizontalPadding * 0.5f)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = horizontalPadding)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = titleFontSize
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = artistFontSize
                ),
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (screenWidth > 480) {
            Spacer(modifier = Modifier.width(8.dp))
        }

        IconButton (
            onClick = onDownloadClick,
            enabled = !isDownloadDisabled,
            modifier = Modifier.size(iconSize + 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                modifier = Modifier.size(iconSize)
            )
        }
    }
}