package com.example.tubesmobdev.ui.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel
import com.example.tubesmobdev.util.rememberDominantColor

@Composable
fun ProfileHeaderContent(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile = viewModel.profile
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val baseUrl = "http://34.101.226.132:3000/uploads/profile-picture/"
    val photoUrl = profile?.profilePhoto?.let { baseUrl + it } ?: ""
    Log.d("URL", photoUrl)
    val painter: Painter = rememberAsyncImagePainter(photoUrl)
    val dominantColor: Color = rememberDominantColor(painter.toString())
    val topGradient = Brush.verticalGradient(
        colors = listOf(dominantColor, Color.Transparent),
        startY = 0f,
        endY = 1200f
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(800.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(topGradient)
        )
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Text(
                    text = "Error: $errorMessage",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            profile != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profile.location,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsColumn(
                            value = viewModel.allSongsCount,
                            label = "SONGS"
                        )
                        StatsColumn(
                            value = viewModel.likedSongsCount,
                            label = "LIKED"
                        )
                        StatsColumn(
                            value = viewModel.listenedSongsCount,
                            label = "LISTENED"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsColumn(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            fontSize = 22.sp,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
