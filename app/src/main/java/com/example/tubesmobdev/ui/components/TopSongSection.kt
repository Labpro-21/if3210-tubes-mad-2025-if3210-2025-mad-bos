package com.example.tubesmobdev.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.util.ProfileUtil
import androidx.compose.foundation.lazy.LazyRow

@Composable
fun TopSongSection(
    onChartClick: (String) -> Unit,
    location: String?,
    connectionStatus: ConnectivityStatus?
) {
    val countryName = location?.let { ProfileUtil.getCountryName(it) } ?: ""

    Log.d("debug", "TopSongSection: " + countryName + " " + location)


    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.width(150.dp)) {
                TopSongCard(
                    title = "Top 50",
                    subtitle = "GLOBAL",
                    gradient = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1d7d75), Color(0xFF1d4c6a), Color(0xFF1e3264))
                    ),
                    onClick = { onChartClick("global") }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your daily update of the most played tracks right now - Global.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (
            location != null &&
            countryName != "Negara tidak tersedia"
        ) {
            item {
                Column(modifier = Modifier.width(150.dp)) {
                    TopSongCard(
                        title = "Top 10",
                        subtitle = countryName.uppercase(),
                        gradient = Brush.verticalGradient(
                            colors = listOf(Color(0xFFf16975), Color(0xFFec1e32))
                        ),
                        onClick = { onChartClick(location) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your daily update of the most played tracks right now - $countryName.",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.width(150.dp)) {
                TopSongCard(
                    title = "Your Mix",
                    subtitle = "Updated daily for you",
                    gradient = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFF311B92))
                    ),
                    onClick = { onChartClick("recomendation") }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fresh picks just for you. Dive into your daily mix of the most played tracks.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}
