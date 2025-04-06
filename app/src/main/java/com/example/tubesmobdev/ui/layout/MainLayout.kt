package com.example.tubesmobdev.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tubesmobdev.ui.components.BottomNavigationBar
import com.example.tubesmobdev.ui.components.MiniPlayerBar
import com.example.tubesmobdev.ui.components.ScreenHeader
import com.example.tubesmobdev.ui.viewmodel.PlayerViewModel
import com.example.tubesmobdev.ui.library.LibraryScreen
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(outerNavController: NavController) {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val progress by playerViewModel.progress.collectAsState()

    var topBarContent by remember  { mutableStateOf<@Composable () -> Unit>({}) }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    var sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = { topBarContent() },
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = {}
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "library"
            ) {
                composable("library") {
                    topBarContent = {
                        ScreenHeader("Library", actions = {
                            IconButton (onClick = { isSheetOpen = true }) {
                                Icon(Icons.Default.Add, "Add")
                            }
                        })
                    }
                    LibraryScreen(
                        navController = navController,
                        onSongClick = { playerViewModel.playSong(it) },
                        isSheetOpen = isSheetOpen,
                        sheetState = sheetState,
                        onCloseSheet = { isSheetOpen = false },
                        onShowSnackbar = { message -> snackbarMessage = message }
                    )
                }

                composable("home") {
                    topBarContent = {
                        ScreenHeader("Home")
                    }
                    HomeScreen(navController = outerNavController, onSongClick = { playerViewModel.playSong(it) },)
                }

                composable("profile") {
                    topBarContent = {
                        ScreenHeader("Profile")
                    }
                    ProfileScreen(navController = navController)
                }
            }

            currentSong?.let { song ->
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    MiniPlayerBar(
                        song = song,
                        isPlaying = isPlaying,
                        progress = progress,
                        onTogglePlayPause = { playerViewModel.togglePlayPause() },
                        onAddClicked = { playerViewModel.toggleLike() },
                        onSwipeLeft = { playerViewModel.playNext() },
                        onSwipeRight = { playerViewModel.playPrevious() }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

