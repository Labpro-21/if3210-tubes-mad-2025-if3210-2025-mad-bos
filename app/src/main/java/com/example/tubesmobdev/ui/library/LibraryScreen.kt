package com.example.tubesmobdev.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tubesmobdev.ui.components.AddSongDrawer
import com.example.tubesmobdev.ui.components.BottomNavigationBar
import com.example.tubesmobdev.ui.components.ScreenHeader
import com.example.tubesmobdev.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, viewModel: LibraryViewModel = hiltViewModel()) {

    val tabs = listOf("All", "Liked")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val allSongs by viewModel.songs.collectAsState()
    val likedSongs by viewModel.likedSongs.collectAsState()
    val songsToShow = if (selectedTabIndex == 0) allSongs else likedSongs
    LaunchedEffect (snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold (
        topBar = { ScreenHeader("Library", actions = {
            IconButton (onClick = { isSheetOpen = true }) {
                Icon(Icons.Default.Add, "Add")
            }
        }) },

        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = {}
    ) {
            paddingValues ->
        Box (
            modifier = Modifier.fillMaxSize().padding(paddingValues),

            ) {

            Column {
                Box(
                    contentAlignment = Alignment.TopStart,
                    modifier = Modifier
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp, top = 10.dp)
                        .width(150.dp)

                ){
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        divider = {},
                        indicator = {}

                    ) {
                        tabs.forEachIndexed{index, title ->
                            Tab(
                                onClick = { selectedTabIndex = index },
                                selected = selectedTabIndex == index,
                                modifier = Modifier
                                    .background(Color.Transparent)
                                    .padding(end = 10.dp)
                                    .clip(shape = RoundedCornerShape(30.dp))
                                    .background(if (selectedTabIndex != index) Color(0xff212121) else MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontSize = 13.sp,
                                    color = if (selectedTabIndex != index) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xff212121))


                SongRecyclerView(songs = songsToShow)
            }

            if (isSheetOpen){
                AddSongDrawer(
                    sheetState = sheetState,
                    onDismissRequest = { isSheetOpen = false },
                    onClose = { isSheetOpen = false },
                    onResult = { result: Result<Unit> ->
                        snackbarMessage = if (result.isSuccess) {
                            "Lagu berhasil ditambahkan"
                        } else {
                            "Gagal menambahkan lagu"
                        }
                        isSheetOpen = false
                    }
                )
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
