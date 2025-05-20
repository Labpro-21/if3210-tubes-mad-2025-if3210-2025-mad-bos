package com.example.tubesmobdev.ui.library.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.R

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit,
    private val onDeleteClick: ((Song) -> Unit)? = null,
    private val onEditClick: ((Song) -> Unit)? = null,
    private val onAddQueueClick: ((Song) -> Unit)? = null ,
): RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.songImage)
        val title: TextView = itemView.findViewById(R.id.songTitle)
        val artist: TextView = itemView.findViewById(R.id.songArtist)
        val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        if (song.coverUrl.isNullOrEmpty()) {
            holder.image.setImageResource(R.drawable.music)
        } else {
            Glide.with(holder.itemView.context)
                .load(song.coverUrl)
                .placeholder(R.drawable.music)
                .into(holder.image)
        }
        holder.itemView.setOnClickListener {
            onItemClick(songs[position])
        }
        if ((onEditClick != null || onDeleteClick != null) && !song.isOnline) {
            holder.menuIcon.visibility = View.VISIBLE
            holder.menuIcon.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                val menu = popup.menu

                if (onEditClick != null) {
                    menu.add("Edit")
                }
                if (onDeleteClick != null) {
                    menu.add("Delete")
                }
                if (onAddQueueClick != null){
                    menu.add("Add to queue")
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Edit" -> {
                            onEditClick?.invoke(song)
                            true
                        }
                        "Delete" -> {
                            onDeleteClick?.invoke(song)
                            true
                        }
                        "Add to queue" -> {
                            onAddQueueClick?.invoke(song)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()


            }
        } else {
            holder.menuIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

}

