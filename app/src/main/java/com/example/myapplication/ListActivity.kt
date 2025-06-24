package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListActivity : AppCompatActivity() {
    
    private lateinit var tvReceived: TextView
    private lateinit var listView: ListView
    private lateinit var btnReturn: Button
    private lateinit var btnShowHistory: Button
    private lateinit var btnClearHistory: Button
    
    // SharedPreferences相关常量
    private companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_DATA = "saved_data"
        const val KEY_EDIT_HISTORY = "edit_history"
    }
    
    private lateinit var sharedPreferences: SharedPreferences
    private var isShowingHistory = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        initViews()
        setupData()
        setupClickListeners()
    }
    
    private fun initViews() {
        tvReceived = findViewById(R.id.tvReceived)
        listView = findViewById(R.id.listView)
        btnReturn = findViewById(R.id.btnReturn)
        btnShowHistory = findViewById(R.id.btnShowHistory)
        btnClearHistory = findViewById(R.id.btnClearHistory)
    }
    
    private fun setupData() {
        // 获取从主画面传递过来的数据
        val receivedData = intent.getStringExtra("data") ?: "无数据"
        tvReceived.text = "接收到的数据: $receivedData"
        
        // 创建示例列表数据
        val listData = mutableListOf<String>()
        listData.add("原始数据: $receivedData")
        listData.add("数据长度: ${receivedData.length}")
        listData.add("数据类型: 字符串")
        listData.add("创建时间: ${System.currentTimeMillis()}")
        listData.add("状态: 已接收")
        
        // 设置ListView适配器
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)
        listView.adapter = adapter
        
        // 设置列表项点击事件
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = listData[position]
            // 将选中的项目返回给主画面
            val resultIntent = Intent()
            resultIntent.putExtra("selected_item", selectedItem)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
    
    private fun setupClickListeners() {
        btnReturn.setOnClickListener {
            // 直接返回，不传递数据
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        
        btnShowHistory.setOnClickListener {
            if (isShowingHistory) {
                setupData() // 显示原始数据
                btnShowHistory.text = "显示编辑历史"
                isShowingHistory = false
            } else {
                showEditHistory() // 显示编辑历史
                btnShowHistory.text = "显示原始数据"
                isShowingHistory = true
            }
        }
        
        btnClearHistory.setOnClickListener {
            clearEditHistory()
        }
    }
    
    /**
     * 显示编辑历史数据
     */
    private fun showEditHistory() {
        val editHistory = sharedPreferences.getString(KEY_EDIT_HISTORY, null)
        val savedData = sharedPreferences.getString(KEY_DATA, "无保存数据")
        
        tvReceived.text = "SharedPreferences中的数据: $savedData"
        
        val historyList = mutableListOf<String>()
        
        if (editHistory.isNullOrEmpty()) {
            historyList.add("暂无编辑历史")
        } else {
            historyList.add("=== 编辑历史记录 ===")
            val historyEntries = editHistory.split("\n")
            historyList.addAll(historyEntries)
        }
        
        // 添加SharedPreferences统计信息
        historyList.add("")
        historyList.add("=== SharedPreferences统计 ===")
        val saveCount = sharedPreferences.getInt("save_count", 0)
        val saveTime = sharedPreferences.getLong("save_time", 0)
        historyList.add("保存次数: $saveCount")
        if (saveTime > 0) {
            val timeStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(saveTime))
            historyList.add("最后保存时间: $timeStr")
        }
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listView.adapter = adapter
        
        // 设置列表项点击事件
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = historyList[position]
            if (selectedItem.isNotEmpty() && !selectedItem.startsWith("===")) {
                // 将选中的项目返回给主画面
                val resultIntent = Intent()
                resultIntent.putExtra("selected_item", selectedItem)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
    
    /**
     * 清除编辑历史
     */
    private fun clearEditHistory() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_EDIT_HISTORY)
        editor.apply()
        
        Toast.makeText(this, "编辑历史已清除", Toast.LENGTH_SHORT).show()
        
        if (isShowingHistory) {
            showEditHistory() // 刷新显示
        }
    }
}