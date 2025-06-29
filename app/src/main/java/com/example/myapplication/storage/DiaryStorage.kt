package com.example.myapplication.storage

import android.content.Context
import android.util.Log
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
 */
class DiaryStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "DiaryStorage"
        private const val DIARY_DIR = "diaries"
        private const val DIARY_LIST_FILE = "diary_list.json"
    }
    
    private val gson = Gson()
    private val diaryDir: File
    private val diaryListFile: File
    
    init {
        diaryDir = File(context.filesDir, DIARY_DIR)
        if (!diaryDir.exists()) {
            diaryDir.mkdirs()
        }
        diaryListFile = File(diaryDir, DIARY_LIST_FILE)
    }
    
    /**
     * 保存日记
     */
    fun saveDiary(diary: Diary): Boolean {
        return try {
            // 保存单个日记文件
            val diaryFile = File(diaryDir, "${diary.id}.json")
            FileWriter(diaryFile).use { writer ->
                gson.toJson(diary, writer)
            }
            
            // 更新日记列表
            val diaryList = loadDiaryList().toMutableList()
            val existingIndex = diaryList.indexOfFirst { it.id == diary.id }
            if (existingIndex >= 0) {
                diaryList[existingIndex] = diary
            } else {
                diaryList.add(diary)
            }
            saveDiaryList(diaryList)
            
            Log.d(TAG, "日记保存成功: ${diary.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "日记保存失败", e)
            false
        }
    }
    
    /**
     * 删除日记
     */
    fun deleteDiary(diaryId: String): Boolean {
        return try {
            // 删除单个日记文件
            val diaryFile = File(diaryDir, "${diaryId}.json")
            if (diaryFile.exists()) {
                diaryFile.delete()
            }
            
            // 从日记列表中移除
            val diaryList = loadDiaryList().toMutableList()
            diaryList.removeAll { it.id == diaryId }
            saveDiaryList(diaryList)
            
            Log.d(TAG, "日记删除成功: $diaryId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "日记删除失败", e)
            false
        }
    }
    
    /**
     * 加载日记列表
     */
    fun loadDiaryList(): List<Diary> {
        return try {
            if (!diaryListFile.exists()) {
                Log.d(TAG, "日记列表文件不存在，返回空列表")
                return emptyList()
            }
            
            FileReader(diaryListFile).use { reader ->
                val type = object : TypeToken<List<Diary>>() {}.type
                val diaryList: List<Diary> = gson.fromJson(reader, type) ?: emptyList()
                Log.d(TAG, "日记列表加载成功，共${diaryList.size}条")
                diaryList.sortedByDescending { it.createTime }
            }
        } catch (e: Exception) {
            Log.e(TAG, "日记列表加载失败", e)
            emptyList()
        }
    }
    
    /**
     * 保存日记列表
     */
    private fun saveDiaryList(diaryList: List<Diary>) {
        try {
            FileWriter(diaryListFile).use { writer ->
                gson.toJson(diaryList, writer)
            }
            Log.d(TAG, "日记列表保存成功，共${diaryList.size}条")
        } catch (e: Exception) {
            Log.e(TAG, "日记列表保存失败", e)
        }
    }
    
    /**
     * 根据日期范围筛选日记
     */
    fun getDiariesByDateRange(startDate: String, endDate: String): List<Diary> {
        val allDiaries = loadDiaryList()
        return allDiaries.filter { diary ->
            diary.date >= startDate && diary.date <= endDate
        }
    }
    
    /**
     * 根据关键词搜索日记
     */
    fun searchDiaries(keyword: String, diaries: List<Diary> = loadDiaryList()): List<Diary> {
        if (keyword.isBlank()) return diaries
        
        return diaries.filter { diary ->
            diary.title.contains(keyword, ignoreCase = true) ||
            diary.content.contains(keyword, ignoreCase = true) ||
            diary.tags.any { it.contains(keyword, ignoreCase = true) }
        }
    }
    
    /**
     * 获取今天的日记
     */
    fun getTodayDiaries(): List<Diary> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return loadDiaryList().filter { it.date == today }
    }
    
    /**
     * 获取本周的日记
     */
    fun getWeekDiaries(): List<Diary> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        return getDiariesByDateRange(startDate, endDate)
    }
    
    /**
     * 获取本月的日记
     */
    fun getMonthDiaries(): List<Diary> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        return getDiariesByDateRange(startDate, endDate)
    }
}