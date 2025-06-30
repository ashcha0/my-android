package com.example.myapplication.utils

import android.content.Context
import android.util.Log
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.database.LogDao
import com.example.myapplication.model.LogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 日志管理器
 * 统一管理应用的日志记录，支持本地存储和系统日志输出
 */
class LogManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "[MyDiaryApp] LogManager"
        private const val MAX_LOGS_IN_DB = 10000 // 数据库中最大日志条数
        private const val CLEANUP_THRESHOLD = 12000 // 触发清理的阈值
        
        @Volatile
        private var INSTANCE: LogManager? = null
        
        fun getInstance(context: Context): LogManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val databaseHelper: DatabaseHelper
    private val logDao: LogDao
    private val sessionId: String = UUID.randomUUID().toString()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    init {
        databaseHelper = DatabaseHelper(context)
        logDao = LogDao(databaseHelper)
        Log.d(TAG, "日志管理器初始化完成，会话ID: $sessionId")
    }
    
    /**
     * 记录DEBUG级别日志
     */
    fun d(tag: String, message: String, component: String, action: String? = null, details: String? = null) {
        logToSystem(LogEntry.LEVEL_DEBUG, tag, message)
        logToDatabase(LogEntry.LEVEL_DEBUG, tag, message, component, action, details)
    }
    
    /**
     * 记录INFO级别日志
     */
    fun i(tag: String, message: String, component: String, action: String? = null, details: String? = null) {
        logToSystem(LogEntry.LEVEL_INFO, tag, message)
        logToDatabase(LogEntry.LEVEL_INFO, tag, message, component, action, details)
    }
    
    /**
     * 记录WARN级别日志
     */
    fun w(tag: String, message: String, component: String, action: String? = null, details: String? = null) {
        logToSystem(LogEntry.LEVEL_WARN, tag, message)
        logToDatabase(LogEntry.LEVEL_WARN, tag, message, component, action, details)
    }
    
    /**
     * 记录ERROR级别日志
     */
    fun e(tag: String, message: String, component: String, action: String? = null, details: String? = null, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            logToSystem(LogEntry.LEVEL_ERROR, tag, message)
        }
        logToDatabase(LogEntry.LEVEL_ERROR, tag, message, component, action, details)
    }
    
    /**
     * 输出到系统日志
     */
    private fun logToSystem(level: String, tag: String, message: String) {
        when (level) {
            LogEntry.LEVEL_DEBUG -> Log.d(tag, message)
            LogEntry.LEVEL_INFO -> Log.i(tag, message)
            LogEntry.LEVEL_WARN -> Log.w(tag, message)
            LogEntry.LEVEL_ERROR -> Log.e(tag, message)
        }
    }
    
    /**
     * 保存到数据库
     */
    private fun logToDatabase(level: String, tag: String, message: String, component: String, action: String?, details: String?) {
        coroutineScope.launch {
            try {
                val logEntry = LogEntry(
                    level = level,
                    tag = tag,
                    message = message,
                    component = component,
                    action = action,
                    details = details,
                    sessionId = sessionId
                )
                
                logDao.saveLog(logEntry)
                
                // 定期清理旧日志
                cleanupOldLogsIfNeeded()
            } catch (e: Exception) {
                Log.e(TAG, "保存日志到数据库失败: $message", e)
            }
        }
    }
    
    /**
     * 如果需要，清理旧日志
     */
    private suspend fun cleanupOldLogsIfNeeded() {
        try {
            val logCount = logDao.getLogCount()
            if (logCount > CLEANUP_THRESHOLD) {
                // 删除最旧的日志，保留最新的MAX_LOGS_IN_DB条
                val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7天前
                val deletedCount = logDao.deleteLogsBefore(cutoffTime)
                Log.d(TAG, "清理旧日志: 删除了${deletedCount}条记录，当前总数: ${logDao.getLogCount()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理旧日志失败", e)
        }
    }
    
    /**
     * 获取日志列表
     */
    fun getLogs(limit: Int = 1000): List<LogEntry> {
        return try {
            logDao.getAllLogs(limit)
        } catch (e: Exception) {
            Log.e(TAG, "获取日志列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 根据组件获取日志
     */
    fun getLogsByComponent(component: String): List<LogEntry> {
        return try {
            logDao.getLogsByComponent(component)
        } catch (e: Exception) {
            Log.e(TAG, "根据组件获取日志失败: $component", e)
            emptyList()
        }
    }
    
    /**
     * 根据级别获取日志
     */
    fun getLogsByLevel(level: String): List<LogEntry> {
        return try {
            logDao.getLogsByLevel(level)
        } catch (e: Exception) {
            Log.e(TAG, "根据级别获取日志失败: $level", e)
            emptyList()
        }
    }
    
    /**
     * 搜索日志
     */
    fun searchLogs(keyword: String): List<LogEntry> {
        return try {
            logDao.searchLogs(keyword)
        } catch (e: Exception) {
            Log.e(TAG, "搜索日志失败: $keyword", e)
            emptyList()
        }
    }
    
    /**
     * 获取日志统计信息
     */
    fun getLogStatistics(): String {
        return try {
            val totalCount = logDao.getLogCount()
            val debugCount = logDao.getLogsByLevel(LogEntry.LEVEL_DEBUG).size
            val infoCount = logDao.getLogsByLevel(LogEntry.LEVEL_INFO).size
            val warnCount = logDao.getLogsByLevel(LogEntry.LEVEL_WARN).size
            val errorCount = logDao.getLogsByLevel(LogEntry.LEVEL_ERROR).size
            
            "日志统计信息:\n" +
                    "- 总计: $totalCount 条\n" +
                    "- DEBUG: $debugCount 条\n" +
                    "- INFO: $infoCount 条\n" +
                    "- WARN: $warnCount 条\n" +
                    "- ERROR: $errorCount 条\n" +
                    "- 当前会话ID: $sessionId"
        } catch (e: Exception) {
            Log.e(TAG, "获取日志统计信息失败", e)
            "获取日志统计信息失败: ${e.message}"
        }
    }
    
    /**
     * 清空所有日志
     */
    fun clearAllLogs(): Boolean {
        return try {
            val success = logDao.clearAllLogs()
            if (success) {
                Log.d(TAG, "清空所有日志成功")
            } else {
                Log.e(TAG, "清空所有日志失败")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "清空所有日志异常", e)
            false
        }
    }
    
    /**
     * 导出日志为文本格式
     */
    fun exportLogsAsText(limit: Int = 1000): String {
        return try {
            val logs = logDao.getAllLogs(limit)
            val sb = StringBuilder()
            sb.append("=== MyDiaryApp 日志导出 ===\n")
            sb.append("导出时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            sb.append("日志总数: ${logs.size}\n")
            sb.append("会话ID: $sessionId\n")
            sb.append("\n")
            
            for (log in logs) {
                sb.append(log.getFullLogMessage())
                sb.append("\n")
            }
            
            sb.toString()
        } catch (e: Exception) {
            Log.e(TAG, "导出日志失败", e)
            "导出日志失败: ${e.message}"
        }
    }
}