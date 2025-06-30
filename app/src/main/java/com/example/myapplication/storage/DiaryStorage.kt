package com.example.myapplication.storage

import android.content.Context
import android.util.Log
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.database.DiaryDao
import com.example.myapplication.model.Diary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日记本地存储管理类
 * 重构为使用SQLite数据库存储，同时保持向后兼容
 */
class DiaryStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "[MyDiaryApp] DiaryStorage"
        private const val DIARY_DIR = "diaries"
        private const val DIARY_LIST_FILE = "diary_list.json"
    }
    
    private val gson = Gson()
    private val diaryDir: File
    private val diaryListFile: File
    
    // SQLite数据库相关
    private val databaseHelper: DatabaseHelper
    private val diaryDao: DiaryDao
    
    init {
        // 保留原有文件目录结构，用于数据迁移
        diaryDir = File(context.filesDir, DIARY_DIR)
        if (!diaryDir.exists()) {
            diaryDir.mkdirs()
        }
        diaryListFile = File(diaryDir, DIARY_LIST_FILE)
        
        // 初始化SQLite数据库
        databaseHelper = DatabaseHelper(context)
        diaryDao = DiaryDao(databaseHelper)
        
        // 执行数据迁移
        migrateFromJsonToSQLite()
    }
    
    /**
     * 保存日记
     */
    fun saveDiary(diary: Diary): Boolean {
        Log.d(TAG, "开始保存日记: ${diary.title}")
        return diaryDao.saveDiary(diary)
    }
    
    /**
     * 删除日记
     */
    fun deleteDiary(diaryId: String): Boolean {
        Log.d(TAG, "开始删除日记: $diaryId")
        return diaryDao.deleteDiary(diaryId)
    }
    
    /**
     * 加载日记列表
     */
    fun loadDiaryList(): List<Diary> {
        Log.d(TAG, "开始加载日记列表")
        return diaryDao.getAllDiaries()
    }
    
    /**
     * 根据日期范围筛选日记
     */
    fun getDiariesByDateRange(startDate: String, endDate: String): List<Diary> {
        Log.d(TAG, "根据日期范围获取日记: $startDate 到 $endDate")
        return diaryDao.getDiariesByDateRange(startDate, endDate)
    }
    
    /**
     * 根据关键词搜索日记
     */
    fun searchDiaries(keyword: String, diaries: List<Diary> = emptyList()): List<Diary> {
        Log.d(TAG, "搜索日记，关键词: $keyword")
        return diaryDao.searchDiaries(keyword)
    }
    
    /**
     * 获取今天的日记
     */
    fun getTodayDiaries(): List<Diary> {
        Log.d(TAG, "获取今天的日记")
        return diaryDao.getTodayDiaries()
    }
    
    /**
     * 获取本周的日记
     */
    fun getWeekDiaries(): List<Diary> {
        Log.d(TAG, "获取本周的日记")
        return diaryDao.getWeekDiaries()
    }
    
    /**
     * 获取本月的日记
     */
    fun getMonthDiaries(): List<Diary> {
        Log.d(TAG, "获取本月的日记")
        return diaryDao.getMonthDiaries()
    }
    
    /**
     * 数据迁移：从JSON文件迁移到SQLite数据库
     */
    private fun migrateFromJsonToSQLite() {
        Log.d(TAG, "开始检查是否需要数据迁移")
        
        // 检查数据库中是否已有数据
        val existingCount = diaryDao.getDiaryCount()
        if (existingCount > 0) {
            Log.d(TAG, "数据库中已有${existingCount}条日记，跳过迁移")
            return
        }
        
        // 检查是否存在旧的JSON文件
        if (!diaryListFile.exists()) {
            Log.d(TAG, "没有找到旧的JSON文件，无需迁移")
            return
        }
        
        try {
            Log.d(TAG, "开始从JSON文件迁移数据到SQLite数据库")
            
            FileReader(diaryListFile).use { reader ->
                val type = object : TypeToken<List<Diary>>() {}.type
                val diaryList: List<Diary> = gson.fromJson(reader, type) ?: emptyList()
                
                if (diaryList.isNotEmpty()) {
                    Log.d(TAG, "找到${diaryList.size}条日记，开始迁移")
                    
                    var successCount = 0
                    diaryList.forEach { diary ->
                        if (diaryDao.saveDiary(diary)) {
                            successCount++
                        } else {
                            Log.w(TAG, "保存日记失败: ${diary.title}")
                        }
                    }
                    
                    Log.d(TAG, "数据迁移完成，成功迁移${successCount}条日记，共${diaryList.size}条")
                    
                    // 迁移成功后，备份原文件
                    if (successCount == diaryList.size) {
                        val backupFile = File(diaryDir, "diary_list_backup.json")
                        val renamed = diaryListFile.renameTo(backupFile)
                        if (renamed) {
                            Log.d(TAG, "原JSON文件已备份为: ${backupFile.name}")
                        } else {
                            Log.w(TAG, "备份原JSON文件失败")
                        }
                    } else {
                        Log.w(TAG, "部分日记迁移失败，成功${successCount}条，总共${diaryList.size}条")
                    }
                } else {
                    Log.d(TAG, "JSON文件中没有数据，无需迁移")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "数据迁移失败", e)
        }
    }
    
    /**
     * 获取数据库信息
     */
    fun getDatabaseInfo(): String {
        return databaseHelper.getDatabaseInfo()
    }
}