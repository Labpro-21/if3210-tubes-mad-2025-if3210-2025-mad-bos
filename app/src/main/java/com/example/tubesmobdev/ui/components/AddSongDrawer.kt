package com.example.tubesmobdev.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.ui.viewmodel.LibraryViewModel
import com.example.tubesmobdev.util.SongUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongDrawer(
    sheetState: SheetState,
    viewModel: LibraryViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onDismissRequest: () -> Unit,
    onResult: (Result<Unit>) -> Unit
) {
    var songUri by rememberSaveable  { mutableStateOf<Uri?>(null) }
    var imageUri by rememberSaveable  { mutableStateOf<Uri?>(null) }
    var title by rememberSaveable  { mutableStateOf("") }
    var artist by rememberSaveable  { mutableStateOf("") }
    var duration by rememberSaveable  { mutableLongStateOf(0L) }
    val context = LocalContext.current

    val songPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> songUri = uri }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    LaunchedEffect(songUri) {
        songUri?.let { uri ->
            val metadata = SongUtil.getAudioMetadata(context, uri)
            title = metadata.title ?: ""
            artist = metadata.artist ?: ""
            duration = metadata.duration
        }
    }


    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xff212121)
    ) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upload Song", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            UploadBox(
                label = "Upload Photo",
                onClick = { imagePickerLauncher.launch("image/*") },
                uri = imageUri,
                icon = Icons.Default.ImageSearch,
                duration = null
            )

            Spacer(Modifier.width(16.dp))

            UploadBox(
                label = "Upload File",
                onClick = { songPickerLauncher.launch("audio/*") },
                uri = songUri,
                icon = Icons.Default.MusicNote,
                duration = duration
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xff535353),
                unfocusedBorderColor = Color(0xff535353),
            )
        )

        Spacer(modifier = Modifier.height(8.dp))



        OutlinedTextField(
            value = artist,
            onValueChange = { artist = it },
            label = { Text("Artist") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xff535353),
                unfocusedBorderColor = Color(0xff535353),

            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button (
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff535353)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (songUri != null && title.isNotEmpty() && artist.isNotEmpty()) {
                        viewModel.insertSong(songUri!!, title, artist, imageUri) { result ->
                            onResult(result)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
    }
}
