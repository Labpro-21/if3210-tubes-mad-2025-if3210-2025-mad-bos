package com.example.tubesmobdev.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SearchLibraryScreen(
    navController: NavController,
    query: String,
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit,
    onAddQueueClick: (Song) -> Unit,
    onSongDelete: (Song) -> Unit,
    onSongUpdate: (Song) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsState()

    var songToDelete by rememberSaveable  { mutableStateOf<Song?>(null) }
    var songToEdit by rememberSaveable { mutableStateOf<Song?>(null) }

    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(snackbarMessage, errorMessage) {
        snackbarMessage?.let {
            onShowSnackbar(it)
        }

        errorMessage?.let {
            onShowSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect (query) {
        if (query.isNotBlank()) {
            viewModel.searchSongs(query)
        }
    }

    if (query.isBlank()) {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Find your favorites", color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Search everything you've saved, followed, or created.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    } else {
        if (searchResults.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Couldn't find \"$query\"",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Try searching again using a different spelling or keyword.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            SongRecyclerView(
                songs = searchResults,
                onItemClick = onSongClick,
                onDeleteClick = { songToDelete = it },
                onEditClick = { songToEdit = it },
                onAddQueueClick = onAddQueueClick
            )
        }
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

    if (songToEdit != null) {
        AddSongDrawer(
            sheetState = sheetState,
            onDismissRequest = {
                songToEdit = null
                isSheetOpen = false
            },
            onClose = {
                songToEdit = null
                isSheetOpen = false
            },
            onResult = { result: Result<Unit> ->
                snackbarMessage = if (result.isSuccess) {
                    "Lagu berhasil disimpan"
                } else {
                    "Gagal menyimpan lagu"
                }
                songToEdit = null
                isSheetOpen = false
            },
            songToEdit = songToEdit,
            onSongUpdate = { onSongUpdate(it) }
        )
    }
}
