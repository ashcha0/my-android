package com.example.myapplication

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var tvMediaTitle: TextView
    private lateinit var tvMediaInfo: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnSelectFile: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnNext: Button

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isVideoMode = false
    
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)
        
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        videoView = findViewById(R.id.videoView)
        tvMediaTitle = findViewById(R.id.tvMediaTitle)
        tvMediaInfo = findViewById(R.id.tvMediaInfo)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        seekBar = findViewById(R.id.seekBar)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnStop = findViewById(R.id.btnStop)
        btnNext = findViewById(R.id.btnNext)
        
        // 设置默认显示信息
        tvMediaTitle.text = "多媒体播放器"
        tvMediaInfo.text = "请选择要播放的文件"
        tvCurrentTime.text = "00:00"
        tvTotalTime.text = "00:00"
        
        // 设置SeekBar监听器
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (isVideoMode) {
                        videoView.seekTo(progress)
                    } else {
                        mediaPlayer?.seekTo(progress)
                    }
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupClickListeners() {
        btnSelectFile.setOnClickListener {
            Toast.makeText(this, "文件选择功能开发中...", Toast.LENGTH_SHORT).show()
        }
        
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }
        
        btnStop.setOnClickListener {
            stopMedia()
        }
        
        btnPrevious.setOnClickListener {
            Toast.makeText(this, "上一首功能开发中...", Toast.LENGTH_SHORT).show()
        }
        
        btnNext.setOnClickListener {
            Toast.makeText(this, "下一首功能开发中...", Toast.LENGTH_SHORT).show()
        }
    }



    private fun togglePlayPause() {
        if (isVideoMode) {
            if (videoView.isPlaying) {
                videoView.pause()
                isPlaying = false
                btnPlayPause.text = "播放"
                handler.removeCallbacks(updateProgressRunnable)
            } else {
                videoView.start()
                isPlaying = true
                btnPlayPause.text = "暂停"
                handler.post(updateProgressRunnable)
            }
        } else {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    isPlaying = false
                    btnPlayPause.text = "播放"
                    handler.removeCallbacks(updateProgressRunnable)
                } else {
                    it.start()
                    isPlaying = true
                    btnPlayPause.text = "暂停"
                    handler.post(updateProgressRunnable)
                }
            } ?: run {
                Toast.makeText(this, "请先选择媒体文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopMedia() {
        handler.removeCallbacks(updateProgressRunnable)
        
        if (isVideoMode) {
            videoView.stopPlayback()
        } else {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        }
        
        isPlaying = false
        btnPlayPause.text = "播放"
        seekBar.progress = 0
        tvCurrentTime.text = "00:00"
    }

    private fun updateProgress() {
        val currentPosition = if (isVideoMode) {
            videoView.currentPosition
        } else {
            mediaPlayer?.currentPosition ?: 0
        }
        
        seekBar.progress = currentPosition
        tvCurrentTime.text = formatTime(currentPosition)
    }

    private fun formatTime(timeMs: Int): String {
        val minutes = timeMs / 1000 / 60
        val seconds = (timeMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMedia()
    }
}