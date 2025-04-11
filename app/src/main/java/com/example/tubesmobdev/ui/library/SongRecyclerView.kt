package com.example.tubesmobdev.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.ui.library.adapter.SongAdapter


@Composable
fun SongRecyclerView(
    songs: List<Song>,
    onItemClick: (Song) -> Unit,
    onDeleteClick: ((Song) -> Unit)? = null,
    onEditClick: ((Song) -> Unit)? = null,
    onAddQueueClick :(Song) -> Unit,
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .verticalScroll(rememberScrollState()),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SongAdapter(songs, onItemClick, onDeleteClick, onEditClick, onAddQueueClick)
                setHasFixedSize(true)
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? SongAdapter)?.updateSongs(songs)
        }
    )
}
