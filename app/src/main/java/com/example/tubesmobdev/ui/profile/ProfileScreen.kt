package com.example.tubesmobdev.ui.profile

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.ui.components.EditLocationSheet
import com.example.tubesmobdev.ui.components.ErrorStateProfile
import com.example.tubesmobdev.ui.components.StatsColumn
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel
import com.example.tubesmobdev.util.rememberDominantColor

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val profile by viewModel.profile.collectAsState()
    val isSheetOpen = remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val allSongsCount by viewModel.allSongsCount.collectAsState()
    val likedSongsCount by viewModel.likedSongsCount.collectAsState()
    val listenedSongsCount by viewModel.listenedSongsCount.collectAsState()
    val topSong by viewModel.topSong.collectAsState()
    val topArtist by viewModel.topArtist.collectAsState()
    val streakSong by viewModel.streakSong.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val streakRange by viewModel.streakRange.collectAsState()
    val context = LocalContext.current as Activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri.value = uri
        uri?.let {
            viewModel.updateProfilePhoto(context, it)
        }
    }
    val baseUrl = "http://34.101.226.132:3000/uploads/profile-picture/"
    val photoUrl = profile?.profilePhoto?.let { baseUrl + it } ?: ""
    val imagePainter: Painter = rememberAsyncImagePainter(
        model = selectedImageUri.value ?: photoUrl
    )
    val painter: Painter = rememberAsyncImagePainter(photoUrl)
    val dominantColor: Color = rememberDominantColor(painter.toString())

    val topGradient = Brush.verticalGradient(
        colors = listOf(dominantColor, Color.Black),
        startY = 0f,
        endY = 1200f
    )

    val windowSizeClass = calculateWindowSizeClass(context)
    val isLargeScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val connectivityStatus by connectionViewModel.connectivityStatus.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(topGradient)
        )

        when {
            connectivityStatus == ConnectivityStatus.Unavailable -> {
                ErrorStateProfile(
                    message = "No internet connection",
                    onLogout = { viewModel.logout() }
                )
            }

            errorMessage != null -> {
                ErrorStateProfile(
                    message = "Something went wrong when fetch profile data",
                    onLogout = { viewModel.logout() }
                )
            }

            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            profile != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp, horizontal = if (isLargeScreen) 100.dp else 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .widthIn(max = 600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(100.dp))

                        Image(
                            painter = painter,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(if (isLargeScreen) 150.dp else 120.dp)
                                .clip(CircleShape).clickable { launcher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = profile!!.username,
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = profile!!.location,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                isSheetOpen.value = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3E3F3F),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .height(45.dp)
                        ) {
                            Text("Edit Profile")
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.logout()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3E3F3F),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .height(45.dp)
                        ) {
                            Text("Logout")
                        }


                        Spacer(modifier = Modifier.height(50.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatsColumn(allSongsCount, "SONGS", Modifier.weight(1f))
                            StatsColumn(likedSongsCount, "LIKED", Modifier.weight(1f))
                            StatsColumn(listenedSongsCount, "LISTENED", Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                        SoundCapsuleSection(
                            month             = "April 2025",
                            minutesListened   = viewModel.totalListeningMinutes.collectAsState().value,
                            topArtist         = topArtist,
                            topSong           = topSong,
                            streakDays        = streakDays,
                            streakSong        = streakSong,
                            streakRange       = streakRange, // misal "Mar 21–25, 2025"
                            onShareStreak     = { /* panggil share intent */ }
                        )
                    }
                }

            }
        }
    }
    if (isSheetOpen.value) {
        EditLocationSheet(

            onDismiss = { isSheetOpen.value = false },
            onSave = { countryCode ->
                Log.d("Wilson", "Selected country code: $countryCode")
                isSheetOpen.value = false
            }
        )
    }
}