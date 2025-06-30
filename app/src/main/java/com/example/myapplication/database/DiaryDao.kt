package com.example.myapplication.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.myapplication.model.Diary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日记数据访问对象
 * 提供日记数据的增删改查操作
 */
class DiaryDao(private val dbHelper: DatabaseHelper) {
    
    companion object {
        private const val TAG = "[MyDiaryApp] DiaryDao"
    }
    
    private val gson = Gson()
    
    /**
     * 插入或更新日记
     */
    fun saveDiary(diary: Diary): Boolean {
        Log.d(TAG, "开始保存日记: ${diary.title}")
        
        val db = dbHelper.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_DIARY_ID, diary.id)
                put(DatabaseHelper.COLUMN_DIARY_TITLE, diary.title)
                put(DatabaseHelper.COLUMN_DIARY_CONTENT, diary.content)
                put(DatabaseHelper.COLUMN_DIARY_DATE, diary.date)
                put(DatabaseHelper.COLUMN_DIARY_CREATE_TIME, diary.createTime)
                put(DatabaseHelper.COLUMN_DIARY_UPDATE_TIME, diary.updateTime)
                put(DatabaseHelper.COLUMN_DIARY_MOOD, diary.mood)
                put(DatabaseHelper.COLUMN_DIARY_WEATHER, diary.weather)
                put(DatabaseHelper.COLUMN_DIARY_TAGS, gson.toJson(diary.tags))
                put(DatabaseHelper.COLUMN_DIARY_IS_REMINDER, if (diary.isReminder) 1 else 0)
                put(DatabaseHelper.COLUMN_DIARY_REMINDER_TIME, diary.reminderTime)
            }
            
            val result = db.insertWithOnConflict(
                DatabaseHelper.TABLE_DIARIES,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            
            val success = result != -1L
            if (success) {
                Log.d(TAG, "日记保存成功: ${diary.title}")
            } else {
                Log.e(TAG, "日记保存失败: ${diary.title}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "保存日记时发生异常: ${diary.title}", e)
            false
        }
    }
    
    /**
     * 根据ID删除日记
     */
    fun deleteDiary(diaryId: String): Boolean {
        Log.d(TAG, "开始删除日记: $diaryId")
        
        val db = dbHelper.writableDatabase
        return try {
            val deletedRows = db.delete(
                DatabaseHelper.TABLE_DIARIES,
                "${DatabaseHelper.COLUMN_DIARY_ID} = ?",
                arrayOf(diaryId)
            )
            
            val success = deletedRows > 0
            if (success) {
                Log.d(TAG, "日记删除成功: $diaryId")
            } else {
                Log.w(TAG, "日记删除失败，可能不存在: $diaryId")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "删除日记时发生异常: $diaryId", e)
            false
        }
    }
    
    /**
     * 根据ID获取日记
     */
    fun getDiaryById(diaryId: String): Diary? {
        Log.d(TAG, "根据ID获取日记: $diaryId")
        
        val db = dbHelper.readableDatabase
        return try {
            db.query(
                DatabaseHelper.TABLE_DIARIES,
                null,
                "${DatabaseHelper.COLUMN_DIARY_ID} = ?",
                arrayOf(diaryId),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val diary = cursorToDiary(cursor)
                    Log.d(TAG, "找到日记: ${diary.title}")
                    diary
                } else {
                    Log.w(TAG, "未找到日记: $diaryId")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "根据ID获取日记时发生异常: $diaryId", e)
            null
        }
    }
    
    /**
     * 获取所有日记，按创建时间倒序排列
     */
    fun getAllDiaries(): List<Diary> {
        Log.d(TAG, "获取所有日记")
        
        val db = dbHelper.readableDatabase
        val diaries = mutableListOf<Diary>()
        
        return try {
            db.query(
                DatabaseHelper.TABLE_DIARIES,
                null,
                null,
                null,
                null,
                null,
                "${DatabaseHelper.COLUMN_DIARY_CREATE_TIME} DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    diaries.add(cursorToDiary(cursor))
                }
            }
            
            Log.d(TAG, "获取所有日记成功，共${diaries.size}条")
            diaries
        } catch (e: Exception) {
            Log.e(TAG, "获取所有日记时发生异常", e)
            emptyList()
        }
    }
    
    /**
     * 根据日期范围获取日记
     */
    fun getDiariesByDateRange(startDate: String, endDate: String): List<Diary> {
        Log.d(TAG, "根据日期范围获取日记: $startDate 到 $endDate")
        
        val db = dbHelper.readableDatabase
        val diaries = mutableListOf<Diary>()
        
        return try {
            db.query(
                DatabaseHelper.TABLE_DIARIES,
                null,
                "${DatabaseHelper.COLUMN_DIARY_DATE} >= ? AND ${DatabaseHelper.COLUMN_DIARY_DATE} <= ?",
                arrayOf(startDate, endDate),
                null,
                null,
                "${DatabaseHelper.COLUMN_DIARY_CREATE_TIME} DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    diaries.add(cursorToDiary(cursor))
                }
            }
            
            Log.d(TAG, "根据日期范围获取日记成功，共${diaries.size}条")
            diaries
        } catch (e: Exception) {
            Log.e(TAG, "根据日期范围获取日记时发生异常", e)
            emptyList()
        }
    }
    
    /**
     * 搜索日记（根据标题、内容、标签）
     */
    fun searchDiaries(keyword: String): List<Diary> {
        if (keyword.isBlank()) {
            return getAllDiaries()
        }
        
        Log.d(TAG, "搜索日记，关键词: $keyword")
        
        val db = dbHelper.readableDatabase
        val diaries = mutableListOf<Diary>()
        
        return try {
            val searchPattern = "%$keyword%"
            db.query(
                DatabaseHelper.TABLE_DIARIES,
                null,
                "${DatabaseHelper.COLUMN_DIARY_TITLE} LIKE ? OR ${DatabaseHelper.COLUMN_DIARY_CONTENT} LIKE ? OR ${DatabaseHelper.COLUMN_DIARY_TAGS} LIKE ?",
                arrayOf(searchPattern, searchPattern, searchPattern),
                null,
                null,
                "${DatabaseHelper.COLUMN_DIARY_CREATE_TIME} DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    diaries.add(cursorToDiary(cursor))
                }
            }
            
            Log.d(TAG, "搜索日记成功，关键词: $keyword，共${diaries.size}条")
            diaries
        } catch (e: Exception) {
            Log.e(TAG, "搜索日记时发生异常，关键词: $keyword", e)
            emptyList()
        }
    }
    
    /**
     * 获取今天的日记
     */
    fun getTodayDiaries(): List<Diary> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d(TAG, "获取今天的日记: $today")
        return getDiariesByDateRange(today, today)
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
        
        Log.d(TAG, "获取本周的日记: $startDate 到 $endDate")
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
        
        Log.d(TAG, "获取本月的日记: $startDate 到 $endDate")
        return getDiariesByDateRange(startDate, endDate)
    }
    
    /**
     * 获取日记总数
     */
    fun getDiaryCount(): Int {
        val db = dbHelper.readableDatabase
        return try {
            db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_DIARIES}", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    Log.d(TAG, "日记总数: $count")
                    count
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取日记总数时发生异常", e)
            0
        }
    }
    
    /**
     * 清空所有日记
     */
    fun clearAllDiaries(): Boolean {
        Log.d(TAG, "开始清空所有日记")
        
        val db = dbHelper.writableDatabase
        return try {
            val deletedRows = db.delete(DatabaseHelper.TABLE_DIARIES, null, null)
            Log.d(TAG, "清空所有日记成功，删除了${deletedRows}条记录")
            true
        } catch (e: Exception) {
            Log.e(TAG, "清空所有日记时发生异常", e)
            false
        }
    }
    
    /**
     * 将Cursor转换为Diary对象
     */
    private fun cursorToDiary(cursor: Cursor): Diary {
        val tagsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_TAGS))
        val tags = try {
            if (tagsJson.isNullOrEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(tagsJson, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "解析标签JSON失败: $tagsJson", e)
            emptyList()
        }
        
        return Diary(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_CONTENT)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_DATE)),
            createTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_CREATE_TIME)),
            updateTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_UPDATE_TIME)),
            mood = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_MOOD)),
            weather = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_WEATHER)),
            tags = tags,
            isReminder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_IS_REMINDER)) == 1,
            reminderTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_REMINDER_TIME))
        )
    }
}