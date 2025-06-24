package com.example.myapplication

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.MusicAdapter
import com.example.myapplication.model.Music
import com.example.myapplication.service.MusicService
import com.example.myapplication.utils.MusicScanner

class MusicPlayerActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MusicPlayerActivity"
        private const val UPDATE_INTERVAL = 1000L
    }
    
    // UI组件
    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvNowPlayingTitle: TextView
    private lateinit var tvNowPlayingArtist: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var rvMusicList: RecyclerView
    
    // 数据和服务
    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var musicAdapter: MusicAdapter
    private var musicList: List<Music> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            scanMusicFiles()
        } else {
            Toast.makeText(this, "需要存储权限才能扫描音乐文件", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            
            // 设置音乐列表
            musicService?.setMusicList(musicList)
            
            // 开始更新UI
            startUpdatingUI()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        
        initViews()
        setupClickListeners()
        setupRecyclerView()
        
        // 启动音乐服务
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        // 请求权限并扫描音乐
        requestPermissionsAndScanMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopUpdatingUI()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnPrevious = findViewById(R.id.btn_previous)
        btnNext = findViewById(R.id.btn_next)
        seekBar = findViewById(R.id.seek_bar)
        tvNowPlayingTitle = findViewById(R.id.tv_now_playing_title)
        tvNowPlayingArtist = findViewById(R.id.tv_now_playing_artist)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        rvMusicList = findViewById(R.id.rv_music_list)
    }
    
    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }
        
        btnPrevious.setOnClickListener {
            musicService?.playPrevious()
        }
        
        btnNext.setOnClickListener {
            musicService?.playNext()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.seekTo(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(musicList)
        rvMusicList.layoutManager = LinearLayoutManager(this)
        rvMusicList.adapter = musicAdapter
        
        musicAdapter.setOnItemClickListener { music, position ->
            musicService?.playMusic(music, position)
            musicAdapter.updatePlayingStatus(position)
        }
    }
    
    private fun requestPermissionsAndScanMusic() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allPermissionsGranted) {
            scanMusicFiles()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    private fun scanMusicFiles() {
        val scanner = MusicScanner(this)
        musicList = scanner.scanMusicFiles()
        
        musicAdapter.updateMusicList(musicList)
        musicService?.setMusicList(musicList)
        
        if (musicList.isEmpty()) {
            Toast.makeText(this, "未找到音乐文件", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "找到 ${musicList.size} 首音乐", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun togglePlayPause() {
        musicService?.let { service ->
            if (service.isPlaying()) {
                service.pauseMusic()
            } else {
                service.resumeMusic()
            }
        }
    }
    
    private fun startUpdatingUI() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateUI()
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
        handler.post(updateRunnable!!)
    }
    
    private fun stopUpdatingUI() {
        updateRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
    
    private fun updateUI() {
        musicService?.let { service ->
            // 更新当前播放信息
            val currentMusic = service.getCurrentMusic()
            if (currentMusic != null) {
                tvNowPlayingTitle.text = currentMusic.title
                tvNowPlayingArtist.text = currentMusic.artist
                
                // 更新播放按钮状态
                if (service.isPlaying()) {
                    btnPlayPause.setImageResource(R.drawable.ic_pause)
                } else {
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                }
                
                // 更新进度条
                val currentPosition = service.getCurrentPosition()
                val duration = service.getDuration()
                
                if (duration > 0) {
                    seekBar.max = duration
                    seekBar.progress = currentPosition
                    
                    tvCurrentTime.text = formatTime(currentPosition)
                    tvTotalTime.text = formatTime(duration)
                }
                
                // 更新列表中的播放状态
                val playingPosition = service.getCurrentMusicPosition()
                musicAdapter.updatePlayingStatus(playingPosition)
            } else {
                tvNowPlayingTitle.text = "未播放"
                tvNowPlayingArtist.text = "-"
                btnPlayPause.setImageResource(R.drawable.ic_play)
                seekBar.progress = 0
                tvCurrentTime.text = "00:00"
                tvTotalTime.text = "00:00"
            }
        }
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}