package com.example.tubesmobdev.ui.layout

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.service.ConnectivityStatus
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tubesmobdev.ui.components.BottomNavigationBar
import com.example.tubesmobdev.ui.components.FullPlayerScreen
import com.example.tubesmobdev.ui.components.MiniPlayerBar
import com.example.tubesmobdev.ui.components.ScreenHeader
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.library.LibraryScreen
import com.example.tubesmobdev.ui.profile.ProfileScreen
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel
import com.example.tubesmobdev.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(outerNavController: NavController) {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    // Global navigation view model that also exposes connectivity status
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val connectivityStatus by navigationViewModel.connectivityStatus.collectAsState()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val progress by playerViewModel.progress.collectAsState()

    var topBarContent by remember  { mutableStateOf<@Composable () -> Unit>({}) }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    LaunchedEffect(connectivityStatus) {
        if (connectivityStatus is ConnectivityStatus.Unavailable) {
            snackbarHostState.showSnackbar("No internet connection")
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(
        topBar = { topBarContent() },
        bottomBar = { BottomNavigationBar(navController) },
//        mau diatas apa dibawah??
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        })
                    }
                    LibraryScreen(
                        navController = navController,
                        onSongClick = { playerViewModel.playSong(it) },
                        onSongDelete = { playerViewModel.stopIfPlaying(it) },
                        onSongUpdate = { playerViewModel.updateCurrentSongIfMatches(it) },
                        isSheetOpen = isSheetOpen,
                        sheetState = sheetState,
                        onCloseSheet = { isSheetOpen = false },
                        onShowSnackbar = { message -> snackbarMessage = message }
                    )
                }

                composable("home") {
                    topBarContent = { ScreenHeader("Home") }
                    HomeScreen(
                        navController = outerNavController,
                        onSongClick = { playerViewModel.playSong(it) }
                    )
                }

                composable("profile") {
                    topBarContent = { ScreenHeader("Profile") }
                    ProfileScreen(navController = navController)
                }
                composable("fullplayer") {
                    topBarContent = {}
                    currentSong?.let { song ->
                        FullPlayerScreen(
                            song = song,
                            isPlaying = isPlaying,
                            progress = progress,
                            onTogglePlayPause = { playerViewModel.togglePlayPause() },
                            onAddClicked = { playerViewModel.toggleLike() },
                            onSkipPrevious = { playerViewModel.playPrevious() },
                            onSkipNext = { playerViewModel.playNext() },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != "fullplayer") {
                currentSong?.let { song ->
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clickable {
                                navController.navigate("fullplayer")
                            }
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
