package com.example.tubesmobdev.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel
import com.example.tubesmobdev.util.formatMonthYear

@Composable
fun TimeListenedScreen(
    month: String,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(month) {
        viewModel.fetchDailyListeningData(month)
    }
    val formattedMonth = formatMonthYear(month)

    val dailyData by viewModel.dailyListeningMinutes.collectAsState()
    val totalMinutes = dailyData.sumOf { it.third } / 60000
    val average = if (dailyData.isNotEmpty()) totalMinutes / dailyData.size else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = formattedMonth, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                append("You listened to music for ")
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF41D18D),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("$totalMinutes minutes")
                }
                append(" this month.")
            },
            fontSize = 32.sp,
            lineHeight = 38.sp,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Daily average: $average min", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        DailyChart(dailyData)
    }
}

@Composable
fun DailyChart(data: List<Triple<Int, Int, Long>> ) {
    val max = data.maxOfOrNull { it.second } ?: 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.DarkGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            data.forEach { (day, minutes) ->
                val barHeightRatio = minutes.toFloat() / max
                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .height((120 * barHeightRatio).dp)
                            .width(8.dp)
                            .background(Color(0xFF41D18D), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = day.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}