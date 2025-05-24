package com.example.tubesmobdev.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onAddQueueClick:((Song) -> Unit)? = null,
    onDeleteQueueClick: ((Int) -> Unit)? = null,
    onDeleteQueueAllClick: (() -> Unit)? = null,
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SongAdapter(songs, onItemClick, onDeleteClick, onEditClick, onAddQueueClick, onDeleteQueueClick, onDeleteQueueAllClick)
                setHasFixedSize(true)
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? SongAdapter)?.updateSongs(songs)
        }
    )
}
