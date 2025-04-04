package com.example.tubesmobdev.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.ui.home.adapter.SongAdapter
import com.example.tubesmobdev.R


@Composable
fun SongRecyclerView(
    songs: List<Song>,
    onItemClick: (Song) -> Unit,
    isHorizontal: Boolean = false,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.padding(top = 10.dp),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = if (isHorizontal) {
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                } else {
                    LinearLayoutManager(context)
                }
                adapter = SongAdapter(
                    songs,
                    onItemClick,
                    layoutRes = if (isHorizontal) R.layout.home_item_song else R.layout.item_song
                )
                setHasFixedSize(true)
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? SongAdapter)?.updateSongs(songs)
        }
    )
}
