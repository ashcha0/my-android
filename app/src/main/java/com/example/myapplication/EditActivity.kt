package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditActivity : AppCompatActivity() {
    
    private lateinit var tvOriginalData: TextView
    private lateinit var etData: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)
        
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
        tvOriginalData = findViewById(R.id.tvOriginalData)
        etData = findViewById(R.id.etData)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
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
    }
}