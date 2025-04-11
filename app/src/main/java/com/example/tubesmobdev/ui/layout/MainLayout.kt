package com.example.tubesmobdev.ui.layout

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.ui.components.*
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.library.LibraryScreen
import com.example.tubesmobdev.ui.library.SearchLibraryScreen
import com.example.tubesmobdev.ui.profile.ProfileHeaderContent
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.PlayerViewModel
import com.example.tubesmobdev.util.rememberDominantColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
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

    var topBarContent by remember { mutableStateOf<@Composable () -> Unit>({}) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current as Activity
    val windowSizeClass = calculateWindowSizeClass(context)

    val dominantColor = rememberDominantColor(currentSong?.coverUrl ?: "").copy(alpha = 0.9f)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact && currentRoute != "fullplayer") {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(240.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    SideNavigation(navController)

                    currentSong?.let {
                        MiniPlayerBar(
                            song = it,
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Scaffold(
                    topBar = { topBarContent() }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                topBarContent = { ScreenHeader("New Songs") }
                                HomeScreen(
                                    navController = outerNavController,
                                    onHomeSongClick = {
                                        playerViewModel.playSong(it)
                                        navController.navigate("fullplayer")
                                    },
                                    onSongClick = { playerViewModel.playSong(it) }
                                )
                            }
                            composable("library") {
                                topBarContent = {
                                    ScreenHeader("Library", actions = {
                                        IconButton(onClick = { navController.navigate("searchLibrary") }) {
                                            Icon(Icons.Default.Search, contentDescription = "Search")
                                        }
                                        IconButton(onClick = { isSheetOpen = true }) {
                                            Icon(Icons.Default.Add, contentDescription = "Add")
                                        }
                                    })
                                }
                                LibraryScreen(
                                    navController,
                                    onSongClick = { playerViewModel.playSong(it) },
                                    onSongDelete = { playerViewModel.stopIfPlaying(it) },
                                    onSongUpdate = { playerViewModel.updateCurrentSongIfMatches(it) },
                                    isSheetOpen = isSheetOpen,
                                    sheetState = sheetState,
                                    onCloseSheet = { isSheetOpen = false },
                                    onShowSnackbar = { snackbarMessage = it },
                                    onAddQueueClick = { playerViewModel.addQueue(it) }
                                )
                            }
                            composable("profile") {
                                topBarContent = { ProfileHeaderContent() }
                            }
                            composable("fullplayer") {
                                topBarContent = {
                                    ScreenHeader(
                                        isMainMenu = false,
                                        title = "fullplayer",
                                        onBack = { navController.popBackStack() },
                                        dominantColor = dominantColor
                                    )
                                }
                                currentSong?.let {
                                    FullPlayerScreen(
                                        song = it,
                                        isPlaying = isPlaying,
                                        progress = progress,
                                        onTogglePlayPause = { playerViewModel.togglePlayPause() },
                                        onAddClicked = { playerViewModel.toggleLike() },
                                        onSkipPrevious = { playerViewModel.playPrevious() },
                                        onSkipNext = { playerViewModel.playNext() },
                                        repeatMode = repeatMode,
                                        onToggleShuffle = { playerViewModel.toggleShuffle() },
                                        onCycleRepeat = { playerViewModel.cycleRepeatMode() },
                                        isShuffle = isShuffle,
                                        onSeekTo = { playerViewModel.seekToPosition(it) },
                                        onSwipeLeft = { playerViewModel.playNext() },
                                        onSwipeRight = { playerViewModel.playPrevious() }
                                    )
                                }
                            }
                            composable("searchLibrary") {
                                topBarContent = {
                                    SearchTopBar(
                                        query = searchQuery,
                                        onQueryChange = { searchQuery = it },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                SearchLibraryScreen(
                                    navController,
                                    query = searchQuery,
                                    onSongClick = { playerViewModel.playSong(it) },
                                    onSongDelete = { playerViewModel.stopIfPlaying(it) },
                                    onSongUpdate = { playerViewModel.updateCurrentSongIfMatches(it) },
                                    onShowSnackbar = { snackbarMessage = it },
                                    onAddQueueClick = { playerViewModel.addQueue(it) }
                                )
                            }
                        }

                        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact && currentRoute != "fullplayer") {
                            currentSong?.let {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .clickable { navController.navigate("fullplayer") }
                                ) {
                                    MiniPlayerBar(
                                        song = it,
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
                    }
                }
            }
        }
    }
}
