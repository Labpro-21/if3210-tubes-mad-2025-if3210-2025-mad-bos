package com.example.tubesmobdev.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.example.tubesmobdev.ui.components.SearchTopBar
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.library.LibraryScreen
import com.example.tubesmobdev.ui.library.SearchLibraryScreen
import com.example.tubesmobdev.ui.profile.ProfileScreen
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.PlayerViewModel
import com.example.tubesmobdev.util.rememberDominantColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(outerNavController: NavController) {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val connectionViewModel: ConnectionViewModel = hiltViewModel()
    val connectivityStatus by connectionViewModel.connectivityStatus.collectAsState()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val isShuffle by playerViewModel.isShuffle.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val progress by playerViewModel.progress.collectAsState()

    var topBarContent by remember  { mutableStateOf<@Composable () -> Unit>({}) }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val dominantColor = rememberDominantColor(currentSong?.coverUrl ?: "").copy(alpha = 0.9f)

    var searchQuery by rememberSaveable { mutableStateOf("") }

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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("library") {
                    topBarContent = {
                        ScreenHeader("Library", actions = {
                            IconButton(onClick = { navController.navigate("searchLibrary") }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                            IconButton(onClick = { isSheetOpen = true }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add"
                                )
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
                        onShowSnackbar = { message -> snackbarMessage = message },
                        onAddQueueClick = {playerViewModel.addQueue(it)}
                    )
                }

                composable("home") {
                    topBarContent = { ScreenHeader("New Songs") }
                    HomeScreen(
                        navController = outerNavController,
                        onHomeSongClick = { song ->
                            playerViewModel.playSong(song)
                            navController.navigate("fullplayer")
                        },
                        onSongClick = { playerViewModel.playSong(it) }
                    )
                }


                composable("profile") {
                    topBarContent = { ScreenHeader("Profile") }
                    ProfileScreen(navController = navController)
                }
                composable("fullplayer") {
                    topBarContent = {
                        ScreenHeader(
                            isMainMenu = false,
                            title = "fullplayer",
                            onBack = { navController.popBackStack() },
                            dominantColor = dominantColor)
                    }
                    currentSong?.let { song ->
                        FullPlayerScreen(
                            song = song,
                            isPlaying = isPlaying,
                            progress = progress,
                            onTogglePlayPause = { playerViewModel.togglePlayPause() },
                            onAddClicked = { playerViewModel.toggleLike() },
                            onSkipPrevious = { playerViewModel.playPrevious() },
                            onSkipNext = { playerViewModel.playNext() },
                            repeatMode = repeatMode,
                            onToggleShuffle = {playerViewModel.toggleShuffle()},
                            onCycleRepeat = {playerViewModel.cycleRepeatMode()},
                            isShuffle = isShuffle,
                            onSeekTo = { newProgress ->
                                playerViewModel.seekToPosition(newProgress)
                            }
                        )
                    }
                }
                composable("searchLibrary") {
                    topBarContent = {
                        SearchTopBar (
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    SearchLibraryScreen(
                        navController = navController,
                        query = searchQuery,
                        onSongClick = { playerViewModel.playSong(it) },
                        onSongDelete = { playerViewModel.stopIfPlaying(it) },
                        onSongUpdate = { playerViewModel.updateCurrentSongIfMatches(it) },
                        onShowSnackbar = { message -> snackbarMessage = message },
                        onAddQueueClick = {playerViewModel.addQueue(it)}
                    )
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
