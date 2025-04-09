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
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.ui.components.AddSongDrawer
import com.example.tubesmobdev.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit,
    isSheetOpen: Boolean,
    sheetState: SheetState,
    onCloseSheet: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {

    val tabs = listOf("All", "Liked")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val allSongs by viewModel.songs.collectAsState()
    val likedSongs by viewModel.likedSongs.collectAsState()
    val songsToShow = if (selectedTabIndex == 0) allSongs else likedSongs
    val errorMessage by viewModel.errorMessage.collectAsState()

    var songToDelete by remember { mutableStateOf<Song?>(null) }


    LaunchedEffect(snackbarMessage, errorMessage) {
        snackbarMessage?.let {
            onShowSnackbar(it)
        }

        errorMessage?.let {
            onShowSnackbar(it)
            viewModel.clearError()
        }
    }

    Box (
        modifier = Modifier.fillMaxSize(),
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
            SongRecyclerView(songs = songsToShow, onSongClick,  onDeleteClick = { songToDelete = it })
        }
        if (isSheetOpen){
            AddSongDrawer(
                sheetState = sheetState,
                onDismissRequest = onCloseSheet,
                onClose = onCloseSheet,
                onResult = { result: Result<Unit> ->
                    snackbarMessage = if (result.isSuccess) {
                        "Lagu berhasil ditambahkan"
                    } else {
                        "Gagal menambahkan lagu"
                    }
                    onCloseSheet()
                }
            )
        }

        songToDelete?.let { song ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { songToDelete = null },
                title = { Text("Hapus Lagu") },
                text = { Text("Apakah kamu yakin ingin menghapus '${song.title}'?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        viewModel.deleteSong(song)
                        songToDelete = null
                        snackbarMessage = "Lagu dihapus"
                    }) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { songToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }

    }


}
