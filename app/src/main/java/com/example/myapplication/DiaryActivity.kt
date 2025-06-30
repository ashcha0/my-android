package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.DiaryAdapter
import com.example.myapplication.model.Diary
import com.example.myapplication.model.ViewMode
import com.example.myapplication.storage.DiaryStorage
import com.example.myapplication.utils.ReminderManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 * 日记主界面
 */
class DiaryActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DiaryActivity"
        const val REQUEST_CODE_ADD_DIARY = 1001
        const val REQUEST_CODE_EDIT_DIARY = 1002
    }
    
    private lateinit var rvDiaries: RecyclerView
    private lateinit var fabAddDiary: FloatingActionButton
    private lateinit var tvEmptyView: TextView
    private lateinit var spinnerViewMode: Spinner
    private lateinit var btnDateFilter: Button
    private lateinit var progressLoading: ProgressBar
    
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var diaryStorage: DiaryStorage
    private lateinit var reminderManager: ReminderManager
    
    private val allDiaries = mutableListOf<Diary>()
    private val filteredDiaries = mutableListOf<Diary>()
    
    private var currentViewMode = ViewMode.ALL
    private var selectedDate: String? = null
    private var searchKeyword = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)
        
        initViews()
        initServices()
        setupRecyclerView()
        setupViewModeSpinner()
        setupClickListeners()
        loadDiaries()
        
        // 重新设置现有的提醒
        restoreExistingReminders()
    }
    
    private fun initViews() {
        rvDiaries = findViewById(R.id.rv_diaries)
        fabAddDiary = findViewById(R.id.fab_add_diary)
        tvEmptyView = findViewById(R.id.tv_empty_view)
        spinnerViewMode = findViewById(R.id.spinner_view_mode)
        btnDateFilter = findViewById(R.id.btn_date_filter)
        progressLoading = findViewById(R.id.progress_loading)
        
        supportActionBar?.title = "我的日记"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun initServices() {
        diaryStorage = DiaryStorage(this)
        reminderManager = ReminderManager(this)
    }
    
    private fun setupRecyclerView() {
        diaryAdapter = DiaryAdapter(
            diaries = mutableListOf(),
            onItemClick = { diary ->
                openDiaryDetail(diary)
            },
            onItemLongClick = { diary ->
                showDiaryOptionsDialog(diary)
            }
        )
        
        rvDiaries.layoutManager = LinearLayoutManager(this)
        rvDiaries.adapter = diaryAdapter
    }
    
    private fun setupViewModeSpinner() {
        val viewModes = arrayOf("全部", "今天", "本周", "本月")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, viewModes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerViewMode.adapter = adapter
        
        spinnerViewMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentViewMode = when (position) {
                    0 -> ViewMode.ALL
                    1 -> ViewMode.TODAY
                    2 -> ViewMode.THIS_WEEK
            3 -> ViewMode.THIS_MONTH
                    else -> ViewMode.ALL
                }
                filterDiaries()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupClickListeners() {
        fabAddDiary.setOnClickListener {
            val intent = Intent(this, DiaryEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_DIARY)
        }
        
        btnDateFilter.setOnClickListener {
            showDatePickerDialog()
        }
    }
    
    private fun loadDiaries() {
        showLoading(true)
        
        thread {
            val diaries = diaryStorage.loadDiaryList()
            
            runOnUiThread {
                showLoading(false)
                allDiaries.clear()
                allDiaries.addAll(diaries)
                filterDiaries()
                
                Log.d(TAG, "日记加载完成，共${allDiaries.size}条")
            }
        }
    }
    
    private fun filterDiaries() {
        filteredDiaries.clear()
        
        var result = allDiaries.toList()
        
        // 按查看模式筛选
        result = when (currentViewMode) {
            ViewMode.ALL -> result
            ViewMode.TODAY -> {
                val today = Calendar.getInstance()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
                result.filter { it.date == todayStr }
            }
            ViewMode.THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                // 获取本周一的日期
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                
                // 获取本周日的日期
                calendar.add(Calendar.DAY_OF_MONTH, 6)
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                
                result.filter { it.date >= startDate && it.date <= endDate }
            }
            ViewMode.THIS_MONTH -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                
                result.filter { it.date >= startDate && it.date <= endDate }
            }
        }
        
        // 按日期筛选
        selectedDate?.let { date ->
            result = result.filter { it.date == date }
        }
        
        // 按关键词搜索
        if (searchKeyword.isNotEmpty()) {
            result = diaryStorage.searchDiaries(searchKeyword, result)
        }
        
        filteredDiaries.addAll(result)
        diaryAdapter.updateData(filteredDiaries)
        updateEmptyView()
    }
    
    private fun updateEmptyView() {
        val isEmpty = filteredDiaries.isEmpty()
        tvEmptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvDiaries.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        // 根据当前筛选条件设置空视图文本
        tvEmptyView.text = when {
            searchKeyword.isNotEmpty() -> "没有找到包含 \"$searchKeyword\" 的日记"
            selectedDate != null -> "$selectedDate 没有日记记录"
            currentViewMode == ViewMode.TODAY -> "今天还没有写日记"
            currentViewMode == ViewMode.THIS_WEEK -> "本周还没有写日记"
            currentViewMode == ViewMode.THIS_MONTH -> "本月还没有写日记"
            else -> "还没有任何日记，点击右下角按钮开始写日记吧！"
        }
    }
    
    /**
     * 重新设置现有日记的提醒
     */
    private fun restoreExistingReminders() {
        thread {
            try {
                val diaries = diaryStorage.loadDiaryList()
                val currentTime = System.currentTimeMillis()
                
                diaries.forEach { diary: Diary ->
                    if (diary.isReminder && diary.reminderTime > currentTime) {
                        // 只重新设置未来的提醒
                        val reminderCalendar = Calendar.getInstance().apply {
                            timeInMillis = diary.reminderTime
                        }
                        
                        reminderManager.setReminder(diary)
                        
                        Log.d(TAG, "重新设置提醒: ${diary.title} at ${reminderCalendar.time}")
                    }
                }
                
                Log.d(TAG, "现有提醒重新设置完成")
            } catch (e: Exception) {
                Log.e(TAG, "重新设置提醒失败", e)
            }
        }
    }
    
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                btnDateFilter.text = selectedDate
                filterDiaries()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "清除") { _, _ ->
            selectedDate = null
            btnDateFilter.text = "选择日期"
            filterDiaries()
        }
        
        datePickerDialog.show()
    }
    
    private fun openDiaryDetail(diary: Diary) {
        val intent = Intent(this, DiaryEditActivity::class.java)
        intent.putExtra(DiaryEditActivity.EXTRA_DIARY, diary)
        startActivityForResult(intent, REQUEST_CODE_EDIT_DIARY)
    }
    
    private fun showDiaryOptionsDialog(diary: Diary) {
        val options = arrayOf("编辑", "删除")
        
        AlertDialog.Builder(this)
            .setTitle(diary.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openDiaryDetail(diary)
                    1 -> showDeleteConfirmDialog(diary)
                }
            }
            .show()
    }
    
    private fun showDeleteConfirmDialog(diary: Diary) {
        AlertDialog.Builder(this)
            .setTitle("删除日记")
            .setMessage("确定要删除日记《${diary.title}》吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteDiary(diary)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteDiary(diary: Diary) {
        thread {
            val success = diaryStorage.deleteDiary(diary.id)
            
            runOnUiThread {
                if (success) {
                    allDiaries.remove(diary)
                    filterDiaries()
                    Toast.makeText(this, "日记删除成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "日记删除失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_diary, menu)
        
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                searchKeyword = newText ?: ""
                filterDiaries()
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ADD_DIARY, REQUEST_CODE_EDIT_DIARY -> {
                    loadDiaries() // 重新加载日记列表
                }
            }
        }
    }
}