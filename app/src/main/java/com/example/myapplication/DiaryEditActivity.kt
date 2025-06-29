package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.Diary
import com.example.myapplication.storage.DiaryStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 * 日记编辑界面
 */
class DiaryEditActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DiaryEditActivity"
        const val EXTRA_DIARY = "extra_diary"
    }
    
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnDate: Button
    private lateinit var spinnerMood: Spinner
    private lateinit var etWeather: EditText
    private lateinit var etTags: EditText
    private lateinit var switchReminder: Switch
    private lateinit var btnReminderTime: Button
    private lateinit var layoutReminder: LinearLayout
    private lateinit var progressSaving: ProgressBar
    
    private lateinit var diaryStorage: DiaryStorage
    
    private var editingDiary: Diary? = null
    private var selectedDate = Calendar.getInstance()
    private var reminderTime: Calendar? = null
    
    private val moodOptions = arrayOf("😊 开心", "😢 难过", "😡 生气", "😴 疲惫", "😍 兴奋", "😌 平静", "🤔 思考", "😎 酷")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_edit)
        
        initViews()
        initServices()
        setupMoodSpinner()
        setupClickListeners()
        loadDiaryData()
    }
    
    private fun initViews() {
        etTitle = findViewById(R.id.et_title)
        etContent = findViewById(R.id.et_content)
        btnDate = findViewById(R.id.btn_date)
        spinnerMood = findViewById(R.id.spinner_mood)
        etWeather = findViewById(R.id.et_weather)
        etTags = findViewById(R.id.et_tags)
        switchReminder = findViewById(R.id.switch_reminder)
        btnReminderTime = findViewById(R.id.btn_reminder_time)
        layoutReminder = findViewById(R.id.layout_reminder)
        progressSaving = findViewById(R.id.progress_saving)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun initServices() {
        diaryStorage = DiaryStorage(this)
    }
    
    private fun setupMoodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moodOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMood.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnDate.setOnClickListener {
            showDatePickerDialog()
        }
        
        btnReminderTime.setOnClickListener {
            showReminderTimePickerDialog()
        }
        
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            layoutReminder.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                reminderTime = null
                btnReminderTime.text = "设置提醒时间"
            }
        }
    }
    
    private fun loadDiaryData() {
        editingDiary = intent.getSerializableExtra(EXTRA_DIARY) as? Diary
        
        if (editingDiary != null) {
            // 编辑模式
            supportActionBar?.title = "编辑日记"
            fillDiaryData(editingDiary!!)
        } else {
            // 新建模式
            supportActionBar?.title = "写日记"
            updateDateButton()
        }
    }
    
    private fun fillDiaryData(diary: Diary) {
        etTitle.setText(diary.title)
        etContent.setText(diary.content)
        
        // 设置日期
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = dateFormat.parse(diary.date)
            date?.let {
                selectedDate.time = it
                updateDateButton()
            }
        } catch (e: Exception) {
            Log.e(TAG, "日期解析失败: ${diary.date}", e)
        }
        
        // 设置心情
        val moodIndex = moodOptions.indexOfFirst { it.contains(diary.mood) }
        if (moodIndex >= 0) {
            spinnerMood.setSelection(moodIndex)
        }
        
        etWeather.setText(diary.weather)
        etTags.setText(diary.tags.joinToString(", "))
        
        // 设置提醒
        switchReminder.isChecked = diary.isReminder
        if (diary.isReminder && diary.reminderTime != null) {
            reminderTime = Calendar.getInstance().apply {
                timeInMillis = diary.reminderTime!!
            }
            updateReminderTimeButton()
        }
    }
    
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateButton()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun showReminderTimePickerDialog() {
        val currentTime = reminderTime ?: Calendar.getInstance()
        
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                if (reminderTime == null) {
                    reminderTime = Calendar.getInstance()
                }
                reminderTime!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                reminderTime!!.set(Calendar.MINUTE, minute)
                updateReminderTimeButton()
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        )
        
        timePickerDialog.show()
    }
    
    private fun updateDateButton() {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        btnDate.text = dateFormat.format(selectedDate.time)
    }
    
    private fun updateReminderTimeButton() {
        reminderTime?.let {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            btnReminderTime.text = "提醒时间: ${timeFormat.format(it.time)}"
        }
    }
    
    private fun saveDiary() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        
        if (title.isEmpty()) {
            etTitle.error = "请输入标题"
            etTitle.requestFocus()
            return
        }
        
        if (content.isEmpty()) {
            etContent.error = "请输入内容"
            etContent.requestFocus()
            return
        }
        
        showSaving(true)
        
        thread {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedMood = moodOptions[spinnerMood.selectedItemPosition]
                val weather = etWeather.text.toString().trim()
                val tagsText = etTags.text.toString().trim()
                val tags = if (tagsText.isNotEmpty()) {
                    tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }
                
                val diary = if (editingDiary != null) {
                    // 更新现有日记
                    editingDiary!!.copy(
                        title = title,
                        content = content,
                        date = dateFormat.format(selectedDate.time),
                        updateTime = System.currentTimeMillis(),
                        mood = selectedMood,
                        weather = weather,
                        tags = tags,
                        isReminder = switchReminder.isChecked,
                        reminderTime = if (switchReminder.isChecked) reminderTime?.timeInMillis ?: 0L else 0L
                    )
                } else {
                    // 创建新日记
                    Diary(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        content = content,
                        date = dateFormat.format(selectedDate.time),
                        createTime = System.currentTimeMillis(),
                        updateTime = System.currentTimeMillis(),
                        mood = selectedMood,
                        weather = weather,
                        tags = tags,
                        isReminder = switchReminder.isChecked,
                        reminderTime = if (switchReminder.isChecked) reminderTime?.timeInMillis ?: 0L else 0L
                    )
                }
                
                val success = diaryStorage.saveDiary(diary)
                
                runOnUiThread {
                    showSaving(false)
                    
                    if (success) {
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "保存日记失败", e)
                runOnUiThread {
                    showSaving(false)
                    Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showSaving(show: Boolean) {
        progressSaving.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_diary_edit, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                saveDiary()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}