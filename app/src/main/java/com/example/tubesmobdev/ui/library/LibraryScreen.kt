package com.example.tubesmobdev.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
    onAddQueueClick: (Song) -> Unit,
    onSongDelete: (Song) -> Unit,
    onSongUpdate: (Song) -> Unit,
    isSheetOpen: Boolean,
    sheetState: SheetState,
    onCloseSheet: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    customTopBar: @Composable (() -> Unit) = {},
    isCompact: Boolean
) {

    val tabs = listOf("All", "Liked")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val allSongs by viewModel.songs.collectAsState()
    val likedSongs by viewModel.likedSongs.collectAsState()
    val songsToShow = if (selectedTabIndex == 0) allSongs else likedSongs
    val errorMessage by viewModel.errorMessage.collectAsState()

    var songToDelete by rememberSaveable  { mutableStateOf<Song?>(null) }
    var songToEdit by rememberSaveable { mutableStateOf<Song?>(null) }

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
        Column(
        ) {
            if (!isCompact){
                customTopBar()
            }
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 10.dp, end = 10.dp, top = 10.dp)
                    .width(150.dp)
            ){
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    divider = {},
                    indicator = {},
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
            Column (
                modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
            ) {
                SongRecyclerView(
                    songs = songsToShow,
                    onItemClick = onSongClick,
                    onDeleteClick = { songToDelete = it },
                    onEditClick = { songToEdit = it },
                    onAddQueueClick = onAddQueueClick
                )
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
                    snackbarMessage = if (result.isSuccess) {
                        "Lagu berhasil disimpan"
                    } else {
                        "Gagal menyimpan lagu"
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
