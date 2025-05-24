package com.example.tubesmobdev.ui.library

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    onSongClick: (Song, List<Song>) -> Unit,
    onAddQueueClick: (Song) -> Unit,
    onDeleteQueueClick: (Int) -> Unit,
    onDeleteQueueAllClick: () -> Unit,
    onSongDelete: (Song) -> Unit,
    onSongUpdate: (Song) -> Unit,
    isSheetOpen: Boolean,
    sheetState: SheetState,
    onCloseSheet: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    customTopBar: @Composable (() -> Unit) = {},
    isCompact: Boolean
) {

    val tabs = listOf("All", "Liked", "Downloaded", "Queue")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current

    val allSongs by viewModel.songs.collectAsState()
    val likedSongs by viewModel.likedSongs.collectAsState()
    val queueSongs by viewModel.currentQueue.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val songsToShow = if (selectedTabIndex == 0) allSongs else if (selectedTabIndex == 1) likedSongs else if (selectedTabIndex == 2) downloadedSongs else queueSongs
    val errorMessage by viewModel.errorMessage.collectAsState()

    var songToDelete by rememberSaveable  { mutableStateOf<Song?>(null) }
    var songToEdit by rememberSaveable { mutableStateOf<Song?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            onShowSnackbar(it)
            viewModel.clearError()
        }
    }


    Box (
        modifier = Modifier.fillMaxSize(),
        ) {
        Column(
        ) {
            if (!isCompact){
                customTopBar()
            }
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 0.dp, end = 0.dp, top = 10.dp)
                    .fillMaxWidth()
            ){
                ScrollableTabRow (
                    selectedTabIndex = selectedTabIndex,
                    divider = {},
                    indicator = {},
                    edgePadding = 8.dp,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            onClick = { selectedTabIndex = index },
                            selected = selectedTabIndex == index,
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(end = 5.dp)
                                .clip(shape = RoundedCornerShape(30.dp))
                                .background(if (selectedTabIndex != index) Color(0xff212121) else MaterialTheme.colorScheme.primaryContainer),

                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 12.sp,
                                color = if (selectedTabIndex != index) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xff212121))
            Column (
                modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
            ) {
                if (selectedTabIndex == 3){
                    SongRecyclerView(
                        songs = songsToShow,
                        onItemClick = { onSongClick(it, queueSongs) },
                        onDeleteClick = { songToDelete = it },
                        onEditClick = { songToEdit = it },
                        onDeleteQueueClick = onDeleteQueueClick,
                        onDeleteQueueAllClick = onDeleteQueueAllClick
                    )
                } else {
                    SongRecyclerView(
                        songs = songsToShow,
                        onItemClick = { onSongClick(it, allSongs) },
                        onDeleteClick = { songToDelete = it },
                        onEditClick = { songToEdit = it },
                        onAddQueueClick = onAddQueueClick,
                    )
                }

            }
        }
        if (isSheetOpen || songToEdit != null) {
            AddSongDrawer(
                sheetState = sheetState,
                onDismissRequest = {
                    songToEdit = null
                    onCloseSheet()
                },
                onClose = {
                    songToEdit = null
                    onCloseSheet()
                },
                onResult = { result: Result<Unit> ->
                    if (result.isSuccess) {
                        onShowSnackbar("Lagu berhasil disimpan")
                    } else {
                        onShowSnackbar("Gagal menyimpan lagu")
                    }
                    songToEdit = null
                    onCloseSheet()
                },
                songToEdit = songToEdit,
                onSongUpdate = { onSongUpdate(it) }
            )
        }


        songToDelete?.let { song ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { songToDelete = null },
                title = { Text("Hapus Lagu") },
                text = { Text("Apakah kamu yakin ingin menghapus '${song.title}'?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        onSongDelete(song)
                        viewModel.deleteSong(song, context)
                        songToDelete = null
                        onShowSnackbar("Lagu dihapus")
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
