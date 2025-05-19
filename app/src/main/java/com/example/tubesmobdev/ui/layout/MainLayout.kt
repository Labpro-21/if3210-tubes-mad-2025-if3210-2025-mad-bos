package com.example.tubesmobdev.ui.layout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Activity
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.data.remote.response.toLocalSong
import com.example.tubesmobdev.service.ConnectivityStatus
import com.example.tubesmobdev.ui.components.*
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.library.LibraryScreen
import com.example.tubesmobdev.ui.library.SearchLibraryScreen
import com.example.tubesmobdev.ui.profile.ProfileScreen
import com.example.tubesmobdev.ui.topsongs.TopSongsScreen
import com.example.tubesmobdev.ui.viewmodel.ConnectionViewModel
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel
import com.example.tubesmobdev.ui.viewmodel.PlayerViewModel
import com.example.tubesmobdev.ui.viewmodel.OnlineSongViewModel
import com.example.tubesmobdev.util.rememberDominantColor
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainLayout(outerNavController: NavController, startDestination: String = "home",  navigationViewModel: NavigationViewModel) {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val connectionViewModel: ConnectionViewModel = hiltViewModel()
    val onlineSongViewModel: OnlineSongViewModel = hiltViewModel()


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

    var wasOffline by remember { mutableStateOf(false) }

    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            snackbarMessage = null
        }
    }

    LaunchedEffect(Unit) {
        navigationViewModel.navigateToFullPlayer.collect {
            Log.d("MainLayout", "Received signal to navigate to fullplayer")
            if (currentRoute != "fullplayer") {
                Log.d("MainLayout", "Navigating to fullplayer from route: $currentRoute")
                navController.navigate("fullplayer")
            } else {
                Log.d("MainLayout", "Already at fullplayer")
            }
        }
    }
    LaunchedEffect(Unit) {
        navigationViewModel.navigateToSongId.collect { songId ->
            Log.d("MainLayout", "Received deep link to songId: $songId")
            try {
                val onlineSong = onlineSongViewModel.getOnlineSongById(songId)
                val song = onlineSong.toLocalSong()
                playerViewModel.playSong(song)
                navController.navigate("fullplayer")
            } catch (e: Exception) {
                Log.e("MainLayout", "Error fetching or playing song: $e")
            }
        }
    }

    LaunchedEffect(connectivityStatus) {
        if (connectivityStatus is ConnectivityStatus.Unavailable) {
            wasOffline = true
            while (connectivityStatus is ConnectivityStatus.Unavailable) {
                val result = snackbarHostState.showSnackbar(
                    "No internet connection",
                    withDismissAction = true
                )
                if (result == SnackbarResult.Dismissed) {
                    kotlinx.coroutines.delay(5000)
                }
            }
        } else if (connectivityStatus is ConnectivityStatus.Available) {
            snackbarHostState.currentSnackbarData?.dismiss()
            if (wasOffline) {
                snackbarHostState.showSnackbar("Back online", withDismissAction = true)
                wasOffline = false
            }
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                if (isCompact) { topBarContent() }
            },
            snackbarHost = { },
            bottomBar = {
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                    BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            val contentPaddingModifier = if (isCompact) {
                Modifier.padding(paddingValues)
            } else {
                Modifier.padding(WindowInsets.systemBars.asPaddingValues())
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(contentPaddingModifier)
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
                            Column(
                                modifier = Modifier
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable("home") {
                                topBarContent = { ScreenHeader("Top Songs") }
                                HomeScreen(
                                    customTopBar = {ScreenHeader("New Songs", isCompact = isCompact)  },
                                    navController = navController,
                                    isCompact = isCompact,
                                    onHomeSongClick = {
                                        playerViewModel.clearCurrentQueue()
                                        playerViewModel.playSong(it)
                                        navController.navigate("fullplayer")

                                    },
                                    onSongClick = {
                                        playerViewModel.clearCurrentQueue()
                                        playerViewModel.playSong(it)
                                    }
                                )
                            }

                            composable("top_songs/{chartCode}") { backStackEntry ->
                                val chartCode = backStackEntry.arguments?.getString("chartCode") ?: "global"

                                topBarContent = {
                                    ScreenHeader(
                                        title = if (connectivityStatus == ConnectivityStatus.Available) {
                                            if (chartCode == "global") "Top 50 Global" else "Top 10 $chartCode"
                                        } else {
                                            "Top Songs"
                                        },
                                        isMainMenu = false,
                                        onBack = { navController.popBackStack() },
                                        dominantColor = if (connectivityStatus != ConnectivityStatus.Available) {
                                            Color(0xFFB0B0B0)
                                        } else if (chartCode == "global") {
                                            Color(0xFF1d7d75)
                                        } else {
                                            Color(0xFFf16975)
                                        }
                                    )
                                }

                                TopSongsScreen(
                                    chartCode = chartCode,
                                    onSongClick = { song, songs ->
                                        playerViewModel.clearCurrentQueue()
                                        playerViewModel.setCurrentQueue(songs)
                                        playerViewModel.playSong(song)
                                    },
                                    onShowSnackbar = { snackbarMessage = it },
                                )
                            }

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
                                            Icon(Icons.Default.Add, contentDescription = "Add")
                                        }
                                        IconButton(onClick = { navController.navigate("qrScan") }) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "Scan QR",
                                                tint = Color.White
                                            )
                                        }
                                    })
                                }
                                LibraryScreen(
                                    navController,
                                    onSongClick = {
                                        playerViewModel.clearCurrentQueue()
                                        playerViewModel.playSong(it)
                                    },
                                    onSongDelete = { playerViewModel.stopIfPlaying(it) },
                                    onSongUpdate = {
                                        playerViewModel.updateCurrentSongIfMatches(
                                            it
                                        )
                                    },
                                    isSheetOpen = isSheetOpen,
                                    sheetState = sheetState,
                                    onCloseSheet = { isSheetOpen = false },
                                    onShowSnackbar = { snackbarMessage = it },
                                    onAddQueueClick = { playerViewModel.addQueue(it) },
                                    isCompact = isCompact,
                                    customTopBar = {
                                        ScreenHeader("Library", isCompact = isCompact, actions = {
                                            IconButton(onClick = { navController.navigate("searchLibrary") }) {
                                                Icon(
                                                    Icons.Default.Search,
                                                    contentDescription = "Search"
                                                )
                                            }
                                            IconButton(onClick = { isSheetOpen = true }) {
                                                Icon(Icons.Default.Add, contentDescription = "Add")
                                            }
                                        })},
                                )
                            }
                            composable("profile") {
                                topBarContent = {}
                                if (isCompact){
                                    ProfileScreen()
                                }else{
                                    ProfileScreen()
                                }
                            }
                            composable("fullplayer") {
                                val context = LocalContext.current
                                topBarContent = {
                                    ScreenHeader(
                                        isMainMenu = false,
                                        title = "fullplayer",
                                        onBack = { navController.popBackStack() },
                                        dominantColor = dominantColor,
                                        actions = {
                                            currentSong?.let { song ->
                                                if (!song.isOnline) {
                                                    IconButton(onClick = { isSheetOpen = true }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                                    }
                                                }
                                            }
                                        }
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
                                        onSwipeRight = { playerViewModel.playPrevious() },
                                        onSongUpdate = {
                                            playerViewModel.updateCurrentSongIfMatches(
                                                it
                                            )
                                        },
                                        isSheetOpen = isSheetOpen,
                                        sheetState = sheetState,
                                        onCloseSheet = { isSheetOpen = false },
                                        onShowSnackbar = { snackbarMessage = it },
                                        isCompact = isCompact,
                                        onQRClicked = {

                                            playerViewModel.shareQRCode(context,it)
                                        },
                                        onShareClicked = {
                                            playerViewModel.shareSong(it)
                                        },
                                        customTopBar = {
                                            ScreenHeader(
                                                isCompact = isCompact,
                                                isMainMenu = false,
                                                title = "",
                                                onBack = { navController.popBackStack() },
                                                dominantColor = Color.Transparent,
                                                iconOnBack = Icons.Filled.ArrowDownward
                                            )
                                        }
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
                                    onSongClick = {
                                        playerViewModel.clearCurrentQueue()
                                        playerViewModel.playSong(it)
                                    },
                                    onSongDelete = { playerViewModel.stopIfPlaying(it) },
                                    onSongUpdate = {
                                        playerViewModel.updateCurrentSongIfMatches(
                                            it
                                        )
                                    },
                                    onShowSnackbar = { snackbarMessage = it },
                                    onAddQueueClick = { playerViewModel.addQueue(it) }
                                )
                            }
                            composable("qrScan") {
                                topBarContent = {Spacer(modifier = Modifier.height(0.dp))}
                                QRScannerScreen(
                                    onScanResult = { result ->
                                        navController.popBackStack()

                                        val songId = result.toLongOrNull()
                                        if (songId != null) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                try {
                                                    val onlineSong = onlineSongViewModel.getOnlineSongById(songId.toString())
                                                    val song = onlineSong.toLocalSong()
                                                    playerViewModel.playSong(song)
                                                    navController.navigate("fullplayer")
                                                    Toast.makeText(context, "Playing song: ${song.title}", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Failed to load song", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Invalid QR format", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }

                        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact && currentRoute != "fullplayer" && currentRoute != "qrScan") {
                            currentSong?.let {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .clickable {  if (isCompact) {
                                            navController.navigate("fullplayer")
                                        } }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}