package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvData: TextView
    private lateinit var btnOpenEdit: Button
    private lateinit var btnOpenList: Button
    private var currentData = "初始值"
    
    // 注册Activity结果回调
    private val editActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val editedData = result.data?.getStringExtra("edited_data")
            if (editedData != null) {
                currentData = editedData
                updateDataDisplay()
            }
        }
    }
    
    private val listActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedItem = result.data?.getStringExtra("selected_item")
            if (selectedItem != null) {
                currentData = selectedItem
                updateDataDisplay()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initViews()
        setupClickListeners()
        updateDataDisplay()
    }
    
    private fun initViews() {
        tvData = findViewById(R.id.tvData)
        btnOpenEdit = findViewById(R.id.btnOpenEdit)
        btnOpenList = findViewById(R.id.btnOpenList)
    }
    
    private fun setupClickListeners() {
        btnOpenEdit.setOnClickListener {
            // 启动编辑画面，传递当前数据
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("data", currentData)
            editActivityLauncher.launch(intent)
        }
        
        btnOpenList.setOnClickListener {
            // 启动列表画面，传递当前数据
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("data", currentData)
            listActivityLauncher.launch(intent)
        }
    }
    
    private fun updateDataDisplay() {
        tvData.text = "当前数据: $currentData"
    }
}