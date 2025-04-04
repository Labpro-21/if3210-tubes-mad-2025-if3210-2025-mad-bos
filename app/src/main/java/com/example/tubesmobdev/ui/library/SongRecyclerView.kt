package com.example.tubesmobdev.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.ui.library.adapter.SongAdapter


/**
 * A composable that wraps a RecyclerView using XML and your SongAdapter.
 * @param songs The list of songs to display.
 * @param onItemClick A lambda invoked when an item is clicked.
 */
@Composable
fun SongRecyclerView(songs: List<Song>, onItemClick: (Song) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize().padding(top = 10.dp),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SongAdapter(songs, onItemClick)
                setHasFixedSize(true)
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? SongAdapter)?.updateSongs(songs)
        }
    )
}
