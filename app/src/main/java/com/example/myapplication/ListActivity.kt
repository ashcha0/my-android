package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListActivity : AppCompatActivity() {
    
    private lateinit var tvReceived: TextView
    private lateinit var listView: ListView
    private lateinit var btnReturn: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initViews()
        setupData()
        setupClickListeners()
    }
    
    private fun initViews() {
        tvReceived = findViewById(R.id.tvReceived)
        listView = findViewById(R.id.listView)
        btnReturn = findViewById(R.id.btnReturn)
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
    }
}