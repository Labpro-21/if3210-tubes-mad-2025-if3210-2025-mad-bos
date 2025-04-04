package com.example.tubesmobdev.ui.home.adapter

import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.R

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit,
    private val layoutRes: Int = R.layout.item_song
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>(), Parcelable {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.songImage)
        val title: TextView = itemView.findViewById(R.id.songTitle)
        val artist: TextView = itemView.findViewById(R.id.songArtist)
    }

    constructor(parcel: Parcel) : this(
        TODO("songs"),
        TODO("onItemClick"),
        parcel.readInt()
    ) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        Glide.with(holder.itemView.context)
            .load(song.coverUrl)
            .into(holder.image)
        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(layoutRes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongAdapter> {
        override fun createFromParcel(parcel: Parcel): SongAdapter {
            return SongAdapter(parcel)
        }

        override fun newArray(size: Int): Array<SongAdapter?> {
            return arrayOfNulls(size)
        }
    }
}
