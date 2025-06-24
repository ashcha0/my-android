package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Music

class MusicAdapter(private var musicList: List<Music>) : 
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {
    
    private var currentPlayingPosition = -1
    private var onItemClickListener: ((Music, Int) -> Unit)? = null
    
    fun setOnItemClickListener(listener: (Music, Int) -> Unit) {
        onItemClickListener = listener
    }
    
    fun updateMusicList(newList: List<Music>) {
        musicList = newList
        notifyDataSetChanged()
    }
    
    fun updatePlayingStatus(position: Int) {
        val oldPosition = currentPlayingPosition
        currentPlayingPosition = position
        
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition)
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.bind(music, position == currentPlayingPosition)
        
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(music, position)
        }
    }
    
    override fun getItemCount(): Int = musicList.size
    
    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_music_title)
        private val tvArtist: TextView = itemView.findViewById(R.id.tv_music_artist)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_music_duration)
        private val ivPlayingIndicator: ImageView = itemView.findViewById(R.id.iv_playing_indicator)
        
        fun bind(music: Music, isPlaying: Boolean) {
            tvTitle.text = music.title
            tvArtist.text = music.artist
            tvDuration.text = music.getFormattedDuration()
            
            if (isPlaying) {
                ivPlayingIndicator.visibility = View.VISIBLE
            } else {
                ivPlayingIndicator.visibility = View.GONE
            }
        }
    }
}