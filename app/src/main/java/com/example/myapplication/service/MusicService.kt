package com.example.myapplication.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.myapplication.model.Music

class MusicService : Service() {
    
    companion object {
        private const val TAG = "MusicService"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusic: Music? = null
    private var musicList: List<Music> = emptyList()
    private var currentPosition = -1
    private var isPlaying = false
    
    private val binder = MusicBinder()
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MusicService created")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        Log.d(TAG, "MusicService destroyed")
    }
    
    fun setMusicList(list: List<Music>) {
        musicList = list
    }
    
    fun playMusic(music: Music, position: Int) {
        try {
            releaseMediaPlayer()
            
            currentMusic = music
            currentPosition = position
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(music.path)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    this@MusicService.isPlaying = true
                    Log.d(TAG, "Playing: ${music.title}")
                }
                setOnCompletionListener {
                    playNext()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing music: ${e.message}")
        }
    }
    
    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
                Log.d(TAG, "Music paused")
            }
        }
    }
    
    fun resumeMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                isPlaying = true
                Log.d(TAG, "Music resumed")
            }
        }
    }
    
    fun playNext() {
        if (musicList.isNotEmpty() && currentPosition < musicList.size - 1) {
            val nextPosition = currentPosition + 1
            playMusic(musicList[nextPosition], nextPosition)
        } else if (musicList.isNotEmpty()) {
            // 循环播放，回到第一首
            playMusic(musicList[0], 0)
        }
    }
    
    fun playPrevious() {
        if (musicList.isNotEmpty() && currentPosition > 0) {
            val previousPosition = currentPosition - 1
            playMusic(musicList[previousPosition], previousPosition)
        } else if (musicList.isNotEmpty()) {
            // 循环播放，跳到最后一首
            val lastPosition = musicList.size - 1
            playMusic(musicList[lastPosition], lastPosition)
        }
    }
    
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }
    
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
    
    fun isPlaying(): Boolean {
        return isPlaying
    }
    
    fun getCurrentMusic(): Music? {
        return currentMusic
    }
    
    fun getCurrentMusicPosition(): Int {
        return currentPosition
    }
    
    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        isPlaying = false
    }
}