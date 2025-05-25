package com.example.tubesmobdev.ui.profile

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.data.model.SoundCapsuleData
import com.example.tubesmobdev.data.model.TopListType
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.ui.components.EditLocationSheet
import com.example.tubesmobdev.ui.components.ErrorStateProfile
import com.example.tubesmobdev.ui.components.StatsColumn
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel
import com.example.tubesmobdev.util.exportCsvFile
import com.example.tubesmobdev.util.rememberDominantColor

fun createImageUri(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )!!
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
) {
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val tempCameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val showPhotoPicker = remember { mutableStateOf(false) }
    val isSheetOpen = remember { mutableStateOf(false) }

    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val allSongsCount by viewModel.allSongsCount.collectAsState()
    val likedSongsCount by viewModel.likedSongsCount.collectAsState()
    val listenedSongsCount by viewModel.listenedSongsCount.collectAsState()
    val capsules by viewModel.capsules.collectAsState()

    val context = LocalContext.current as Activity

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri.value = it
            viewModel.updateProfilePhoto(context, it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraImageUri.value?.let {
                selectedImageUri.value = it
                viewModel.updateProfilePhoto(context, it)
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            tempCameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied, action canceled", Toast.LENGTH_SHORT).show()
        }
    }
    val baseUrl = "http://34.101.226.132:3000/uploads/profile-picture/"
    val photoUrl = profile?.profilePhoto?.let { baseUrl + it } ?: ""
    val imagePainter: Painter = rememberAsyncImagePainter(
        model = selectedImageUri.value ?: photoUrl
    )
    val dominantColor: Color = rememberDominantColor(photoUrl)

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
        Box(modifier = Modifier.matchParentSize().background(topGradient))

        when {
            connectivityStatus == ConnectivityStatus.Unavailable -> ErrorStateProfile("No internet connection") { viewModel.logout() }
            errorMessage != null -> ErrorStateProfile("Something went wrong when fetching profile data") { viewModel.logout() }
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            profile != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp, horizontal = if (isLargeScreen) 100.dp else 24.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(100.dp))

                        Image(
                            painter = imagePainter,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(if (isLargeScreen) 150.dp else 120.dp)
                                .clip(CircleShape)
                                .clickable { showPhotoPicker.value = true },
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(profile!!.username, style = MaterialTheme.typography.headlineSmall, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(profile!!.location, style = MaterialTheme.typography.bodyLarge, fontSize = 16.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { isSheetOpen.value = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3F3F), contentColor = Color.White), modifier = Modifier.width(200.dp).height(45.dp)) {
                            Text("Edit Profile")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.logout() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3F3F), contentColor = Color.White), modifier = Modifier.width(200.dp).height(45.dp)) {
                            Text("Logout")
                        }

                        Spacer(modifier = Modifier.height(50.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatsColumn(allSongsCount, "SONGS", Modifier.weight(1f))
                            StatsColumn(likedSongsCount, "LIKED", Modifier.weight(1f))
                            StatsColumn(listenedSongsCount, "LISTENED", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Sound Capsule",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            IconButton(onClick = {
                                val csvData = buildCsvFromCapsules(capsules)
                                exportCsvFile(context, csvData, "sound_capsule_export.csv")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Capsule",
                                    tint = Color.White
                                )
                            }
                        }
                        SoundCapsuleSection(
                            navController = navController,
                            capsules      = capsules,
                            onArtistClick = { monthYear ->
                                viewModel.fetchMonthlyTopList(monthYear, TopListType.Artist)
                                navController.navigate("topList/Artist/$monthYear")
                            },
                            onSongClick   = { monthYear ->
                                viewModel.fetchMonthlyTopList(monthYear, TopListType.Song)
                                navController.navigate("topList/Song/$monthYear")
                            } ,
                            onTimeListenedClick  = { data ->
                                navController.navigate("timeListened/${data.month}") // atau route yang sesuai
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPhotoPicker.value) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker.value = false },
            title = { Text("Choose Image Source") },
            confirmButton = {
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        val uri = createImageUri(context)
                        tempCameraImageUri.value = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    showPhotoPicker.value = false
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showPhotoPicker.value = false
                }) {
                    Text("Gallery")
                }
            }
        )
    }

    if (isSheetOpen.value) {
        EditLocationSheet(
            onDismiss = { isSheetOpen.value = false },
            onSave = {
                isSheetOpen.value = false
                viewModel.updateLocation(it)
            }
        )
    }
}

fun buildCsvFromCapsules(capsules: List<SoundCapsuleData>): String {
    val header = "month,minutesListened,topArtist,topSong,streakRange,streakTitle"
    val rows = capsules.map {
        val artist = it.topArtist?.artist ?: "-"
        val song = it.topSong?.title ?: "-"
        val range = it.streakRange.ifEmpty { "-" }
        val streakTitle = it.streakSong?.title ?: "-"
        "${it.month},${it.minutesListened},$artist,$song,$range,$streakTitle"
    }
    return (listOf(header) + rows).joinToString("\n")
}

