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
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.net.Uri
import android.provider.MediaStore
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
    private lateinit var btnSelectMusic: Button
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
    
    // 文件选择器
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("MusicPlayer", "文件选择器返回结果: $uri")
        if (uri != null) {
            Log.d("MusicPlayer", "选择了文件，准备添加音乐")
            addMusicFromUri(uri)
        } else {
            Log.d("MusicPlayer", "未选择文件或选择被取消")
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
        btnSelectMusic = findViewById(R.id.btn_select_music)
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
        
        btnSelectMusic.setOnClickListener {
            openFilePicker()
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
    
    private fun openFilePicker() {
        Log.d("MusicPlayer", "启动文件选择器")
        try {
            filePickerLauncher.launch("audio/*")
            Log.d("MusicPlayer", "文件选择器启动成功")
        } catch (e: Exception) {
            Log.e("MusicPlayer", "启动文件选择器失败", e)
            Toast.makeText(this, "无法打开文件选择器: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addMusicFromUri(uri: Uri) {
        try {
            Log.d("MusicPlayer", "开始添加音乐文件: $uri")
            Log.d("MusicPlayer", "URI类型: ${uri.scheme}")
            
            // 检查权限
            val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            
            val hasPermission = ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED
            Log.d("MusicPlayer", "存储权限状态: $hasPermission")
            
            if (!hasPermission) {
                Log.e("MusicPlayer", "缺少必要的存储权限")
                Toast.makeText(this, "需要存储权限才能添加音乐", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 尝试直接从URI获取文件名
            var fileName = ""
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex) ?: ""
                        Log.d("MusicPlayer", "从URI直接获取的文件名: $fileName")
                    }
                }
            }
            
            // 使用特定的音频媒体查询
            val projection = arrayOf(
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE
            )
            
            Log.d("MusicPlayer", "开始查询音频媒体信息")
            val cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            
            Log.d("MusicPlayer", "查询结果: cursor = $cursor")
            
            if (cursor == null) {
                Log.e("MusicPlayer", "查询返回null cursor，尝试使用文件名创建音乐对象")
                
                if (fileName.isNotEmpty()) {
                    // 使用文件名创建基本的Music对象
                    createMusicFromFileName(uri, fileName)
                } else {
                    Toast.makeText(this, "无法访问音乐文件", Toast.LENGTH_SHORT).show()
                }
                return
            }
            
            cursor.use {
                Log.d("MusicPlayer", "cursor count: ${it.count}")
                if (it.moveToFirst()) {
                    Log.d("MusicPlayer", "成功移动到第一行")
                    
                    try {
                        // 尝试获取列索引，如果不存在则使用默认值
                        val titleIndex = try {
                            it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                        } catch (e: Exception) {
                            Log.w("MusicPlayer", "无法获取DISPLAY_NAME列索引", e)
                            -1
                        }
                        
                        val artistIndex = try {
                            it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        } catch (e: Exception) {
                            Log.w("MusicPlayer", "无法获取ARTIST列索引", e)
                            -1
                        }
                        
                        val durationIndex = try {
                            it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        } catch (e: Exception) {
                            Log.w("MusicPlayer", "无法获取DURATION列索引", e)
                            -1
                        }
                        
                        val altTitleIndex = try {
                            it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        } catch (e: Exception) {
                            Log.w("MusicPlayer", "无法获取TITLE列索引", e)
                            -1
                        }
                        
                        Log.d("MusicPlayer", "列索引 - title: $titleIndex, artist: $artistIndex, duration: $durationIndex, altTitle: $altTitleIndex")
                        
                        // 获取数据，如果列不存在则使用默认值
                        val title = if (titleIndex >= 0) it.getString(titleIndex) else fileName
                        val altTitle = if (altTitleIndex >= 0) it.getString(altTitleIndex) else null
                        val artist = if (artistIndex >= 0) it.getString(artistIndex) else "未知艺术家"
                        val duration = if (durationIndex >= 0) it.getLong(durationIndex) else 0L
                        
                        // 使用最佳可用标题
                        val bestTitle = when {
                            !title.isNullOrEmpty() -> title
                            !altTitle.isNullOrEmpty() -> altTitle
                            !fileName.isEmpty() -> fileName
                            else -> "未知歌曲"
                        }
                        
                        Log.d("MusicPlayer", "提取的信息 - title: $bestTitle, artist: $artist, duration: $duration")
                        
                        val music = Music(
                             id = System.currentTimeMillis(),
                             title = bestTitle.removeSuffix(".mp3").removeSuffix(".m4a").removeSuffix(".wav"),
                             artist = artist ?: "未知艺术家",
                             album = "未知专辑",
                             path = uri.toString(),
                             duration = duration,
                             albumId = 0L
                         )
                        
                        Log.d("MusicPlayer", "创建Music对象: $music")
                        
                        // 添加到音乐列表
                        val newMusicList = musicList.toMutableList()
                        newMusicList.add(music)
                        musicList = newMusicList
                        
                        Log.d("MusicPlayer", "更新音乐列表，新列表大小: ${musicList.size}")
                        
                        // 更新服务中的音乐列表
                        musicService?.setMusicList(musicList)
                        Log.d("MusicPlayer", "更新服务中的音乐列表")
                        
                        // 更新适配器
                        musicAdapter.updateMusicList(musicList)
                        Log.d("MusicPlayer", "更新适配器")
                        
                        Toast.makeText(this, "已添加: ${music.title}", Toast.LENGTH_SHORT).show()
                        Log.d("MusicPlayer", "音乐添加成功: ${music.title}")
                    } catch (e: Exception) {
                        Log.e("MusicPlayer", "处理cursor数据时出错", e)
                        
                        // 尝试使用文件名作为备选方案
                        if (fileName.isNotEmpty()) {
                            createMusicFromFileName(uri, fileName)
                        } else {
                            Toast.makeText(this, "处理音乐文件信息失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.w("MusicPlayer", "cursor为空或无法移动到第一行")
                    
                    // 尝试使用文件名作为备选方案
                    if (fileName.isNotEmpty()) {
                        createMusicFromFileName(uri, fileName)
                    } else {
                        Toast.makeText(this, "无法读取音乐文件信息", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "添加音乐失败", e)
            Toast.makeText(this, "添加音乐失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createMusicFromFileName(uri: Uri, fileName: String) {
        Log.d("MusicPlayer", "使用文件名创建音乐对象: $fileName")
        
        val title = fileName.removeSuffix(".mp3").removeSuffix(".m4a").removeSuffix(".wav")
        
        val music = Music(
             id = System.currentTimeMillis(),
             title = title,
             artist = "未知艺术家",
             album = "未知专辑",
             path = uri.toString(),
             duration = 0L,
             albumId = 0L
         )
        
        Log.d("MusicPlayer", "从文件名创建Music对象: $music")
        
        // 添加到音乐列表
        val newMusicList = musicList.toMutableList()
        newMusicList.add(music)
        musicList = newMusicList
        
        // 更新服务中的音乐列表
        musicService?.setMusicList(musicList)
        
        // 更新适配器
        musicAdapter.updateMusicList(musicList)
        
        Toast.makeText(this, "已添加: ${music.title}", Toast.LENGTH_SHORT).show()
        Log.d("MusicPlayer", "从文件名添加音乐成功: ${music.title}")
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}