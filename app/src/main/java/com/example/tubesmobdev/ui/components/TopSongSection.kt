package com.example.tubesmobdev.ui.components

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
import androidx.compose.ui.unit.dp
import com.example.tubesmobdev.util.ProfileUtil
import java.util.Locale

@Composable
fun TopSongSection(onChartClick: (String) -> Unit, location: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.width(100.dp)) {
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
                text = "Top Song Global",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        if (location != null){
            Column(modifier = Modifier.width(100.dp)) {
                TopSongCard(
                    title = "Top 10",
                    subtitle = ProfileUtil.getCountryName(location).uppercase(),
                    gradient = Brush.verticalGradient(
                        colors = listOf(Color(0xFFf16975), Color(0xFFec1e32))
                    ),
                    onClick = { onChartClick(location) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Top Song Local",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

