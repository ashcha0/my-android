package com.example.myapplication

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var listView: ListView
    private lateinit var customAdapter: CustomAdapter
    private val dataList = mutableListOf<ItemData>()
    
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
        setupData()
        setupAdapter()
        setupClickListener()
    }
    
    private fun initViews() {
        listView = findViewById(R.id.listView)
    }
    
    private fun setupData() {
        // 添加示例数据
        dataList.add(ItemData("Android开发", "学习Android应用开发技术", android.R.drawable.ic_menu_camera))
        dataList.add(ItemData("Kotlin编程", "掌握Kotlin编程语言基础", android.R.drawable.ic_menu_edit))
        dataList.add(ItemData("UI设计", "创建美观的用户界面", android.R.drawable.ic_menu_gallery))
        dataList.add(ItemData("数据库操作", "学习SQLite数据库使用", android.R.drawable.ic_menu_manage))
        dataList.add(ItemData("网络编程", "实现网络数据交互功能", android.R.drawable.ic_menu_send))
        dataList.add(ItemData("自定义控件", "开发个性化UI组件", android.R.drawable.ic_menu_preferences))
        dataList.add(ItemData("性能优化", "提升应用运行效率", android.R.drawable.ic_menu_sort_by_size))
        dataList.add(ItemData("测试调试", "确保应用质量和稳定性", android.R.drawable.ic_menu_info_details))
    }
    
    private fun setupAdapter() {
        customAdapter = CustomAdapter(this, dataList)
        listView.adapter = customAdapter
    }
    
    private fun setupClickListener() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = dataList[position]
            Toast.makeText(this, "点击了: ${selectedItem.title}", Toast.LENGTH_SHORT).show()
        }
    }
}