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
            Log.d(TAG, "开始播放音乐: ${music.title}, 路径: ${music.path}")
            releaseMediaPlayer()
            
            currentMusic = music
            currentPosition = position
            
            mediaPlayer = MediaPlayer().apply {
                Log.d(TAG, "创建MediaPlayer实例")
                
                try {
                    // 检查路径类型并设置数据源
                    if (music.path.startsWith("content://")) {
                        Log.d(TAG, "使用URI设置数据源: ${music.path}")
                        setDataSource(this@MusicService, android.net.Uri.parse(music.path))
                    } else {
                        Log.d(TAG, "使用文件路径设置数据源: ${music.path}")
                        setDataSource(music.path)
                    }
                    
                    Log.d(TAG, "开始异步准备MediaPlayer")
                    prepareAsync()
                } catch (e: Exception) {
                    Log.e(TAG, "设置数据源失败", e)
                    throw e
                }
                
                setOnPreparedListener {
                    Log.d(TAG, "MediaPlayer准备完成，开始播放")
                    start()
                    this@MusicService.isPlaying = true
                    Log.d(TAG, "播放开始: ${music.title}")
                }
                
                setOnCompletionListener {
                    Log.d(TAG, "播放完成: ${music.title}")
                    playNext()
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer播放错误: what=$what, extra=$extra, 音乐: ${music.title}")
                    Log.e(TAG, "错误详情 - what含义: ${getErrorMessage(what)}, extra: $extra")
                    this@MusicService.isPlaying = false
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "播放音乐失败: ${music.title}", e)
            this.isPlaying = false
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
            try {
                if (it.isPlaying) {
                    Log.d(TAG, "停止当前播放")
                    it.stop()
                }
                Log.d(TAG, "释放MediaPlayer资源")
                it.release()
            } catch (e: Exception) {
                Log.e(TAG, "释放MediaPlayer时出错", e)
            }
        }
        mediaPlayer = null
        isPlaying = false
    }
    
    private fun getErrorMessage(what: Int): String {
        return when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "MEDIA_ERROR_UNKNOWN"
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MEDIA_ERROR_SERVER_DIED"
            else -> "未知错误码: $what"
        }
    }
}