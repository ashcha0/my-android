package com.example.myapplication.database

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.myapplication.model.LogEntry

/**
 * 日志数据访问对象
 * 提供日志数据的增删改查操作
 */
class LogDao(private val databaseHelper: DatabaseHelper) {
    
    companion object {
        private const val TAG = "[MyDiaryApp] LogDao"
    }
    
    /**
     * 保存日志条目
     */
    fun saveLog(logEntry: LogEntry): Long {
        val db = databaseHelper.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_LOG_TIMESTAMP, logEntry.timestamp)
                put(DatabaseHelper.COLUMN_LOG_LEVEL, logEntry.level)
                put(DatabaseHelper.COLUMN_LOG_TAG, logEntry.tag)
                put(DatabaseHelper.COLUMN_LOG_MESSAGE, logEntry.message)
                put(DatabaseHelper.COLUMN_LOG_COMPONENT, logEntry.component)
                put(DatabaseHelper.COLUMN_LOG_ACTION, logEntry.action)
                put(DatabaseHelper.COLUMN_LOG_DETAILS, logEntry.details)
                put(DatabaseHelper.COLUMN_LOG_USER_ID, logEntry.userId)
                put(DatabaseHelper.COLUMN_LOG_SESSION_ID, logEntry.sessionId)
            }
            
            val id = db.insert(DatabaseHelper.TABLE_LOGS, null, values)
            if (id != -1L) {
                Log.d(TAG, "保存日志成功: ID=$id, Level=${logEntry.level}, Component=${logEntry.component}, Message=${logEntry.message}")
            } else {
                Log.e(TAG, "保存日志失败: ${logEntry.message}")
            }
            id
        } catch (e: Exception) {
            Log.e(TAG, "保存日志异常: ${logEntry.message}", e)
            -1L
        }
    }
    
    /**
     * 批量保存日志条目
     */
    fun saveLogs(logEntries: List<LogEntry>): Int {
        val db = databaseHelper.writableDatabase
        var successCount = 0
        
        db.beginTransaction()
        try {
            for (logEntry in logEntries) {
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_LOG_TIMESTAMP, logEntry.timestamp)
                    put(DatabaseHelper.COLUMN_LOG_LEVEL, logEntry.level)
                    put(DatabaseHelper.COLUMN_LOG_TAG, logEntry.tag)
                    put(DatabaseHelper.COLUMN_LOG_MESSAGE, logEntry.message)
                    put(DatabaseHelper.COLUMN_LOG_COMPONENT, logEntry.component)
                    put(DatabaseHelper.COLUMN_LOG_ACTION, logEntry.action)
                    put(DatabaseHelper.COLUMN_LOG_DETAILS, logEntry.details)
                    put(DatabaseHelper.COLUMN_LOG_USER_ID, logEntry.userId)
                    put(DatabaseHelper.COLUMN_LOG_SESSION_ID, logEntry.sessionId)
                }
                
                val id = db.insert(DatabaseHelper.TABLE_LOGS, null, values)
                if (id != -1L) {
                    successCount++
                }
            }
            db.setTransactionSuccessful()
            Log.d(TAG, "批量保存日志成功: ${successCount}/${logEntries.size}")
        } catch (e: Exception) {
            Log.e(TAG, "批量保存日志失败", e)
        } finally {
            db.endTransaction()
        }
        
        return successCount
    }
    
    /**
     * 根据ID获取日志
     */
    fun getLogById(id: Long): LogEntry? {
        val db = databaseHelper.readableDatabase
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_LOGS,
                null,
                "${DatabaseHelper.COLUMN_LOG_ID} = ?",
                arrayOf(id.toString()),
                null, null, null
            )
            
            cursor.use {
                if (it.moveToFirst()) {
                    val logEntry = cursorToLogEntry(it)
                    Log.d(TAG, "根据ID获取日志成功: $id")
                    logEntry
                } else {
                    Log.w(TAG, "未找到ID为$id 的日志")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "根据ID获取日志失败: $id", e)
            null
        }
    }
    
    /**
     * 获取所有日志（按时间倒序）
     */
    fun getAllLogs(limit: Int = 1000): List<LogEntry> {
        val db = databaseHelper.readableDatabase
        val logs = mutableListOf<LogEntry>()
        
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_LOGS,
                null, null, null, null, null,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} DESC",
                limit.toString()
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    logs.add(cursorToLogEntry(it))
                }
            }
            
            Log.d(TAG, "获取所有日志成功: ${logs.size}条")
            logs
        } catch (e: Exception) {
            Log.e(TAG, "获取所有日志失败", e)
            emptyList()
        }
    }
    
    /**
     * 根据时间范围获取日志
     */
    fun getLogsByTimeRange(startTime: Long, endTime: Long): List<LogEntry> {
        val db = databaseHelper.readableDatabase
        val logs = mutableListOf<LogEntry>()
        
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_LOGS,
                null,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} BETWEEN ? AND ?",
                arrayOf(startTime.toString(), endTime.toString()),
                null, null,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} DESC"
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    logs.add(cursorToLogEntry(it))
                }
            }
            
            Log.d(TAG, "根据时间范围获取日志成功: ${logs.size}条")
            logs
        } catch (e: Exception) {
            Log.e(TAG, "根据时间范围获取日志失败", e)
            emptyList()
        }
    }
    
    /**
     * 根据级别获取日志
     */
    fun getLogsByLevel(level: String): List<LogEntry> {
        val db = databaseHelper.readableDatabase
        val logs = mutableListOf<LogEntry>()
        
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_LOGS,
                null,
                "${DatabaseHelper.COLUMN_LOG_LEVEL} = ?",
                arrayOf(level),
                null, null,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} DESC"
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    logs.add(cursorToLogEntry(it))
                }
            }
            
            Log.d(TAG, "根据级别获取日志成功: ${logs.size}条")
            logs
        } catch (e: Exception) {
            Log.e(TAG, "根据级别获取日志失败", e)
            emptyList()
        }
    }
    
    /**
     * 根据组件获取日志
     */
    fun getLogsByComponent(component: String): List<LogEntry> {
        val db = databaseHelper.readableDatabase
        val logs = mutableListOf<LogEntry>()
        
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_LOGS,
                null,
                "${DatabaseHelper.COLUMN_LOG_COMPONENT} = ?",
                arrayOf(component),
                null, null,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} DESC"
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    logs.add(cursorToLogEntry(it))
                }
            }
            
            Log.d(TAG, "根据组件获取日志成功: ${logs.size}条")
            logs
        } catch (e: Exception) {
            Log.e(TAG, "根据组件获取日志失败", e)
            emptyList()
        }
    }
    
    /**
     * 搜索日志（根据消息内容）
     */
    fun searchLogs(keyword: String): List<LogEntry> {
        val db = databaseHelper.readableDatabase
        val logs = mutableListOf<LogEntry>()
        
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_LOGS,
                null,
                "${DatabaseHelper.COLUMN_LOG_MESSAGE} LIKE ? OR ${DatabaseHelper.COLUMN_LOG_TAG} LIKE ?",
                arrayOf("%$keyword%", "%$keyword%"),
                null, null,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} DESC"
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    logs.add(cursorToLogEntry(it))
                }
            }
            
            Log.d(TAG, "搜索日志成功: 关键词='$keyword', 结果=${logs.size}条")
            logs
        } catch (e: Exception) {
            Log.e(TAG, "搜索日志失败: 关键词='$keyword'", e)
            emptyList()
        }
    }
    
    /**
     * 获取日志总数
     */
    fun getLogCount(): Int {
        val db = databaseHelper.readableDatabase
        return try {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_LOGS}", null)
            cursor.use {
                if (it.moveToFirst()) {
                    val count = it.getInt(0)
                    Log.d(TAG, "获取日志总数: $count")
                    count
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取日志总数失败", e)
            0
        }
    }
    
    /**
     * 删除指定时间之前的日志
     */
    fun deleteLogsBefore(timestamp: Long): Int {
        val db = databaseHelper.writableDatabase
        return try {
            val deletedCount = db.delete(
                DatabaseHelper.TABLE_LOGS,
                "${DatabaseHelper.COLUMN_LOG_TIMESTAMP} < ?",
                arrayOf(timestamp.toString())
            )
            Log.d(TAG, "删除指定时间之前的日志: ${deletedCount}条")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "删除指定时间之前的日志失败", e)
            0
        }
    }
    
    /**
     * 清空所有日志
     */
    fun clearAllLogs(): Boolean {
        val db = databaseHelper.writableDatabase
        return try {
            val deletedCount = db.delete(DatabaseHelper.TABLE_LOGS, null, null)
            Log.d(TAG, "清空所有日志: ${deletedCount}条")
            true
        } catch (e: Exception) {
            Log.e(TAG, "清空所有日志失败", e)
            false
        }
    }
    
    /**
     * 将Cursor转换为LogEntry对象
     */
    private fun cursorToLogEntry(cursor: Cursor): LogEntry {
        return LogEntry(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_ID)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_TIMESTAMP)),
            level = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_LEVEL)),
            tag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_TAG)),
            message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_MESSAGE)),
            component = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_COMPONENT)),
            action = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_ACTION)),
            details = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_DETAILS)),
            userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_USER_ID)),
            sessionId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_SESSION_ID))
        )
    }
}