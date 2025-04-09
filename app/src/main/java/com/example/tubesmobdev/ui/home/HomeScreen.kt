package com.example.tubesmobdev.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit,
    onHomeSongClick: (Song) -> Unit,
) {
    val newestSongs by viewModel.newestSongs.collectAsState()
    val recentlyPlayedSongs by viewModel.recentlyPlayedSongs.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // "New songs" Section (Horizontal)
        Text(
            text = "New songs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SongRecyclerView(
            songs = newestSongs,
            onItemClick = onHomeSongClick,
            isHorizontal = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // "Recently played" Section (Vertical)
        Text(
            text = "Recently played",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // For the vertical list, wrap it with a weight to occupy available space
        SongRecyclerView(
            songs = recentlyPlayedSongs,
            onItemClick = onSongClick,
            isHorizontal = false,
            modifier = Modifier.weight(1f)
        )

        Button(onClick = {
            viewModel.logout()
        }) {
            Text("Klik Saya")
        }
    }
}
