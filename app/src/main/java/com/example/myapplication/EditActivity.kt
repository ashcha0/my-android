package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditActivity : AppCompatActivity() {
    
    private lateinit var tvOriginalData: TextView
    private lateinit var etData: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnSaveToSP: Button
    private lateinit var btnLoadFromSP: Button
    
    // SharedPreferences相关常量
    private companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_DATA = "saved_data"
        const val KEY_EDIT_HISTORY = "edit_history"
    }
    
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)
        
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
        tvOriginalData = findViewById(R.id.tvOriginalData)
        etData = findViewById(R.id.etData)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnSaveToSP = findViewById(R.id.btnSaveToSP)
        btnLoadFromSP = findViewById(R.id.btnLoadFromSP)
    }
    
    private fun setupData() {
        // 获取从主画面传递过来的数据
        val originalData = intent.getStringExtra("data") ?: "初始值"
        tvOriginalData.text = "原始数据: $originalData"
        etData.setText(originalData)
    }
    
    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            // 保存编辑后的数据并返回给主画面
            val editedData = etData.text.toString()
            saveEditHistory(editedData) // 保存编辑历史到SharedPreferences
            val resultIntent = Intent()
            resultIntent.putExtra("edited_data", editedData)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        
        btnCancel.setOnClickListener {
            // 取消编辑，不返回数据
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        
        btnSaveToSP.setOnClickListener {
            saveCurrentDataToSP()
        }
        
        btnLoadFromSP.setOnClickListener {
            loadDataFromSP()
        }
    }
    
    /**
     * 保存当前编辑的数据到SharedPreferences
     */
    private fun saveCurrentDataToSP() {
        val currentText = etData.text.toString()
        if (currentText.isNotEmpty()) {
            val editor = sharedPreferences.edit()
            editor.putString(KEY_DATA, currentText)
            editor.apply()
            Toast.makeText(this, "数据已保存到SharedPreferences", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "请输入要保存的数据", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 从SharedPreferences加载数据到编辑框
     */
    private fun loadDataFromSP() {
        val savedData = sharedPreferences.getString(KEY_DATA, null)
        if (savedData != null) {
            etData.setText(savedData)
            Toast.makeText(this, "数据已从SharedPreferences加载", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "没有找到保存的数据", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 保存编辑历史到SharedPreferences
     */
    private fun saveEditHistory(editedData: String) {
        val currentHistory = sharedPreferences.getString(KEY_EDIT_HISTORY, "")
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val newEntry = "[$timestamp] $editedData"
        val updatedHistory = if (currentHistory.isNullOrEmpty()) {
            newEntry
        } else {
            "$currentHistory\n$newEntry"
        }
        
        val editor = sharedPreferences.edit()
        editor.putString(KEY_EDIT_HISTORY, updatedHistory)
        editor.apply()
    }
}