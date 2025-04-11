package com.example.tubesmobdev.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tubesmobdev.R
import com.example.tubesmobdev.data.model.Song

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit,
    private val onDeleteClick: ((Song) -> Unit)? = null,
    private val onEditClick: ((Song) -> Unit)? = null,
    private val layoutRes: Int = R.layout.item_song
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.songImage)
        val title: TextView = itemView.findViewById(R.id.songTitle)
        val artist: TextView = itemView.findViewById(R.id.songArtist)
        val menuIcon: ImageView? = itemView.findViewById(R.id.menuIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
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
            onItemClick(song)
        }

        holder.menuIcon?.let { icon ->
            if (onEditClick != null || onDeleteClick != null) {
                icon.visibility = View.VISIBLE
                icon.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, view)
                    val menu = popup.menu

                    if (onEditClick != null) {
                        menu.add("Edit")
                    }
                    if (onDeleteClick != null) {
                        menu.add("Delete")
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
                            else -> false
                        }
                    }

                    popup.show()
                }
            } else {
                icon.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
