package com.example.myapplication.model

/**
 * 日志条目数据模型
 * 用于存储应用运行时的日志信息
 */
data class LogEntry(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // DEBUG, INFO, WARN, ERROR
    val tag: String,
    val message: String,
    val component: String, // 组件名称，如 ReminderManager, NotificationHelper 等
    val action: String? = null, // 操作类型，如 SET_REMINDER, SHOW_NOTIFICATION 等
    val details: String? = null, // 详细信息，JSON格式
    val userId: String? = null, // 用户标识（如果需要）
    val sessionId: String? = null // 会话标识（如果需要）
) {
    
    companion object {
        // 日志级别常量
        const val LEVEL_DEBUG = "DEBUG"
        const val LEVEL_INFO = "INFO"
        const val LEVEL_WARN = "WARN"
        const val LEVEL_ERROR = "ERROR"
        
        // 组件名称常量
        const val COMPONENT_REMINDER_MANAGER = "ReminderManager"
        const val COMPONENT_NOTIFICATION_HELPER = "NotificationHelper"
        const val COMPONENT_ALARM_RECEIVER = "AlarmReceiver"
        const val COMPONENT_DIARY_EDIT_ACTIVITY = "DiaryEditActivity"
        const val COMPONENT_DIARY_STORAGE = "DiaryStorage"
        const val COMPONENT_MUSIC_STORAGE = "MusicStorage"
        const val COMPONENT_DATABASE = "Database"
        
        // 操作类型常量
        const val ACTION_SET_REMINDER = "SET_REMINDER"
        const val ACTION_CANCEL_REMINDER = "CANCEL_REMINDER"
        const val ACTION_SHOW_NOTIFICATION = "SHOW_NOTIFICATION"
        const val ACTION_SAVE_DIARY = "SAVE_DIARY"
        const val ACTION_LOAD_DIARY = "LOAD_DIARY"
        const val ACTION_DELETE_DIARY = "DELETE_DIARY"
        const val ACTION_SAVE_MUSIC = "SAVE_MUSIC"
        const val ACTION_LOAD_MUSIC = "LOAD_MUSIC"
        const val ACTION_DATABASE_MIGRATION = "DATABASE_MIGRATION"
        const val ACTION_PERMISSION_CHECK = "PERMISSION_CHECK"
    }
    
    /**
     * 格式化时间戳为可读字符串
     */
    fun getFormattedTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    
    /**
     * 获取完整的日志信息
     */
    fun getFullLogMessage(): String {
        val sb = StringBuilder()
        sb.append("[${getFormattedTimestamp()}] ")
        sb.append("[$level] ")
        sb.append("[$component] ")
        if (action != null) {
            sb.append("[$action] ")
        }
        sb.append("$tag: $message")
        if (details != null) {
            sb.append(" | Details: $details")
        }
        return sb.toString()
    }
}