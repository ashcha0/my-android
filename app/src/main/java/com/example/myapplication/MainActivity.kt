package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvData: TextView
    private lateinit var btnOpenEdit: Button
    private lateinit var btnOpenList: Button
    private lateinit var btnSaveData: Button
    private lateinit var btnLoadData: Button
    private lateinit var btnClearData: Button
    private var currentData = "初始值"
    
    // SharedPreferences相关常量
    private companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_DATA = "saved_data"
        const val KEY_SAVE_TIME = "save_time"
        const val KEY_SAVE_COUNT = "save_count"
    }
    
    private lateinit var sharedPreferences: SharedPreferences
    
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
        
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        initViews()
        setupClickListeners()
        loadDataFromPreferences() // 启动时加载保存的数据
        updateDataDisplay()
    }
    
    private fun initViews() {
        tvData = findViewById(R.id.tvData)
        btnOpenEdit = findViewById(R.id.btnOpenEdit)
        btnOpenList = findViewById(R.id.btnOpenList)
        btnSaveData = findViewById(R.id.btnSaveData)
        btnLoadData = findViewById(R.id.btnLoadData)
        btnClearData = findViewById(R.id.btnClearData)
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
        
        btnSaveData.setOnClickListener {
            saveDataToPreferences()
        }
        
        btnLoadData.setOnClickListener {
            loadDataFromPreferences()
        }
        
        btnClearData.setOnClickListener {
            clearDataFromPreferences()
        }
    }
    
    private fun updateDataDisplay() {
        val saveCount = sharedPreferences.getInt(KEY_SAVE_COUNT, 0)
        val saveTime = sharedPreferences.getLong(KEY_SAVE_TIME, 0)
        val timeStr = if (saveTime > 0) {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(saveTime))
        } else {
            "未保存"
        }
        
        tvData.text = "当前数据: $currentData\n保存次数: $saveCount\n最后保存时间: $timeStr"
    }
    
    /**
     * 保存数据到SharedPreferences
     */
    private fun saveDataToPreferences() {
        val editor = sharedPreferences.edit()
        val currentTime = System.currentTimeMillis()
        val saveCount = sharedPreferences.getInt(KEY_SAVE_COUNT, 0) + 1
        
        editor.putString(KEY_DATA, currentData)
        editor.putLong(KEY_SAVE_TIME, currentTime)
        editor.putInt(KEY_SAVE_COUNT, saveCount)
        
        // 使用apply()异步保存，也可以使用commit()同步保存
        editor.apply()
        
        Toast.makeText(this, "数据已保存到SharedPreferences", Toast.LENGTH_SHORT).show()
        updateDataDisplay()
    }
    
    /**
     * 从SharedPreferences加载数据
     */
    private fun loadDataFromPreferences() {
        val savedData = sharedPreferences.getString(KEY_DATA, null)
        if (savedData != null) {
            currentData = savedData
            Toast.makeText(this, "数据已从SharedPreferences加载", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "没有找到保存的数据", Toast.LENGTH_SHORT).show()
        }
        updateDataDisplay()
    }
    
    /**
     * 清除SharedPreferences中的数据
     */
    private fun clearDataFromPreferences() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_DATA)
        editor.remove(KEY_SAVE_TIME)
        editor.remove(KEY_SAVE_COUNT)
        editor.apply()
        
        currentData = "初始值"
        Toast.makeText(this, "SharedPreferences数据已清除", Toast.LENGTH_SHORT).show()
        updateDataDisplay()
    }
}