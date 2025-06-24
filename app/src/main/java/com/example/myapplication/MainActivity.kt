package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var customAdapter: CustomAdapter
    private val dataList = mutableListOf<ItemData>()
    private lateinit var btnMediaPlayer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupData()
        setupAdapter()
        setupClickListener()
    }
    
    private fun initViews() {
        listView = findViewById(R.id.listView)
        btnMediaPlayer = findViewById(R.id.btnMediaPlayer)
    }
    
    private fun setupData() {
        dataList.clear()
        dataList.add(ItemData("音乐播放", "支持播放本地音频文件", android.R.drawable.ic_media_play))
        dataList.add(ItemData("视频播放", "支持播放本地视频文件", android.R.drawable.ic_media_play))
        dataList.add(ItemData("进度控制", "可以控制播放进度和时间", android.R.drawable.ic_media_ff))
        dataList.add(ItemData("播放控制", "播放、暂停、停止、上一首、下一首", android.R.drawable.ic_media_pause))
        dataList.add(ItemData("文件浏览", "浏览和选择SD卡中的媒体文件", android.R.drawable.ic_menu_gallery))
    }
    
    private fun setupAdapter() {
        customAdapter = CustomAdapter(this, dataList)
        listView.adapter = customAdapter
    }
    
    private fun setupClickListener() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = dataList[position]
            Toast.makeText(this, "点击了: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        
        btnMediaPlayer.setOnClickListener {
            val intent = Intent(this, MediaPlayerActivity::class.java)
            startActivity(intent)
        }
    }
}