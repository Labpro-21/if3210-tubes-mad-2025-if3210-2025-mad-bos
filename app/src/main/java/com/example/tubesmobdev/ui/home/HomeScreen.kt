package com.example.tubesmobdev.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.ui.components.TopSongSection
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.HomeViewModel
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>) -> Unit,
    onHomeSongClick: (Song, List<Song>) -> Unit,
    customTopBar: @Composable (() -> Unit) = {},
    isCompact: Boolean
) {
    val newestSongs by viewModel.newestSongs.collectAsState()
    val recentlyPlayedSongs by viewModel.recentlyPlayedSongs.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()

    val profile by profileViewModel.profile.collectAsState()
    val isLoadingProfile by profileViewModel.isLoading.collectAsState()

    val connectivityStatus by connectionViewModel.connectivityStatus.collectAsState()

    LaunchedEffect (Unit) {
        profileViewModel.fetchProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        if (!isCompact){
            customTopBar()
        }

        Text(
            text = "Charts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 16.dp, bottom = 16.dp)
        )

        if (!isLoadingProfile) {
            TopSongSection   (
                onChartClick = { chartCode ->
                    navController.navigate("top_songs/$chartCode")
                },
                location = profile?.location,
                connectionStatus = connectivityStatus
            )
        }

        Text(
            text = "New Songs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        )

        SongRecyclerView(
            songs = newestSongs,
            onItemClick = { onHomeSongClick(it, allSongs) },
            isHorizontal = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Recently played",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        )

        SongRecyclerView(
            songs = recentlyPlayedSongs,
            onItemClick = {
                onSongClick(it, allSongs)
            },
            isHorizontal = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
