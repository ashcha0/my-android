package com.example.myapplication.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * SQLite数据库帮助类
 * 负责数据库的创建、升级和版本管理
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val TAG = "[MyDiaryApp] DatabaseHelper"
        
        // 数据库信息
        const val DATABASE_NAME = "my_diary_app.db"
        const val DATABASE_VERSION = 1
        
        // 日记表
        const val TABLE_DIARIES = "diaries"
        const val COLUMN_DIARY_ID = "id"
        const val COLUMN_DIARY_TITLE = "title"
        const val COLUMN_DIARY_CONTENT = "content"
        const val COLUMN_DIARY_DATE = "date"
        const val COLUMN_DIARY_CREATE_TIME = "create_time"
        const val COLUMN_DIARY_UPDATE_TIME = "update_time"
        const val COLUMN_DIARY_MOOD = "mood"
        const val COLUMN_DIARY_WEATHER = "weather"
        const val COLUMN_DIARY_TAGS = "tags"
        const val COLUMN_DIARY_IS_REMINDER = "is_reminder"
        const val COLUMN_DIARY_REMINDER_TIME = "reminder_time"
        
        // 音乐表
        const val TABLE_MUSIC = "music"
        const val COLUMN_MUSIC_ID = "id"
        const val COLUMN_MUSIC_TITLE = "title"
        const val COLUMN_MUSIC_ARTIST = "artist"
        const val COLUMN_MUSIC_ALBUM = "album"
        const val COLUMN_MUSIC_DURATION = "duration"
        const val COLUMN_MUSIC_PATH = "path"
        const val COLUMN_MUSIC_ALBUM_ID = "album_id"
        const val COLUMN_MUSIC_ADD_TIME = "add_time"
        
        // 日志表
        const val TABLE_LOGS = "logs"
        const val COLUMN_LOG_ID = "id"
        const val COLUMN_LOG_TIMESTAMP = "timestamp"
        const val COLUMN_LOG_LEVEL = "level"
        const val COLUMN_LOG_TAG = "tag"
        const val COLUMN_LOG_MESSAGE = "message"
        const val COLUMN_LOG_COMPONENT = "component"
        const val COLUMN_LOG_ACTION = "action"
        const val COLUMN_LOG_DETAILS = "details"
        const val COLUMN_LOG_USER_ID = "user_id"
        const val COLUMN_LOG_SESSION_ID = "session_id"
        
        // 创建日记表的SQL语句
        private val CREATE_DIARY_TABLE = """
            CREATE TABLE $TABLE_DIARIES (
                $COLUMN_DIARY_ID TEXT PRIMARY KEY,
                $COLUMN_DIARY_TITLE TEXT NOT NULL,
                $COLUMN_DIARY_CONTENT TEXT NOT NULL,
                $COLUMN_DIARY_DATE TEXT NOT NULL,
                $COLUMN_DIARY_CREATE_TIME INTEGER NOT NULL,
                $COLUMN_DIARY_UPDATE_TIME INTEGER NOT NULL,
                $COLUMN_DIARY_MOOD TEXT,
                $COLUMN_DIARY_WEATHER TEXT,
                $COLUMN_DIARY_TAGS TEXT,
                $COLUMN_DIARY_IS_REMINDER INTEGER NOT NULL DEFAULT 0,
                $COLUMN_DIARY_REMINDER_TIME INTEGER
            )
        """.trimIndent()
        
        // 创建音乐表的SQL语句
        private val CREATE_MUSIC_TABLE = """
            CREATE TABLE $TABLE_MUSIC (
                $COLUMN_MUSIC_ID TEXT PRIMARY KEY,
                $COLUMN_MUSIC_TITLE TEXT NOT NULL,
                $COLUMN_MUSIC_ARTIST TEXT,
                $COLUMN_MUSIC_ALBUM TEXT,
                $COLUMN_MUSIC_DURATION INTEGER NOT NULL,
                $COLUMN_MUSIC_PATH TEXT NOT NULL,
                $COLUMN_MUSIC_ALBUM_ID INTEGER,
                $COLUMN_MUSIC_ADD_TIME INTEGER
            )
        """.trimIndent()
        
        // 创建日志表的SQL语句
        private val CREATE_LOGS_TABLE = """
            CREATE TABLE $TABLE_LOGS (
                $COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LOG_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_LOG_LEVEL TEXT NOT NULL,
                $COLUMN_LOG_TAG TEXT NOT NULL,
                $COLUMN_LOG_MESSAGE TEXT NOT NULL,
                $COLUMN_LOG_COMPONENT TEXT NOT NULL,
                $COLUMN_LOG_ACTION TEXT,
                $COLUMN_LOG_DETAILS TEXT,
                $COLUMN_LOG_USER_ID TEXT,
                $COLUMN_LOG_SESSION_ID TEXT
            )
        """.trimIndent()
        
        // 创建索引
        private val CREATE_INDEX_DIARY_DATE = "CREATE INDEX idx_diary_date ON $TABLE_DIARIES($COLUMN_DIARY_DATE)"
        private val CREATE_INDEX_DIARY_CREATE_TIME = "CREATE INDEX idx_diary_create_time ON $TABLE_DIARIES($COLUMN_DIARY_CREATE_TIME)"
        private val CREATE_INDEX_MUSIC_TITLE = "CREATE INDEX idx_music_title ON $TABLE_MUSIC($COLUMN_MUSIC_TITLE)"
        private val CREATE_INDEX_MUSIC_ARTIST = "CREATE INDEX idx_music_artist ON $TABLE_MUSIC($COLUMN_MUSIC_ARTIST)"
        private val CREATE_INDEX_LOG_TIMESTAMP = "CREATE INDEX idx_log_timestamp ON $TABLE_LOGS($COLUMN_LOG_TIMESTAMP)"
        private val CREATE_INDEX_LOG_COMPONENT = "CREATE INDEX idx_log_component ON $TABLE_LOGS($COLUMN_LOG_COMPONENT)"
        private val CREATE_INDEX_LOG_LEVEL = "CREATE INDEX idx_log_level ON $TABLE_LOGS($COLUMN_LOG_LEVEL)"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "创建数据库，版本: $DATABASE_VERSION")
        
        try {
            // 创建日记表
            db.execSQL(CREATE_DIARY_TABLE)
            Log.d(TAG, "日记表创建成功")
            
            // 创建音乐表
            db.execSQL(CREATE_MUSIC_TABLE)
            Log.d(TAG, "音乐表创建成功")
            
            // 创建日志表
            db.execSQL(CREATE_LOGS_TABLE)
            Log.d(TAG, "日志表创建成功")
            
            // 创建索引
            db.execSQL(CREATE_INDEX_DIARY_DATE)
            db.execSQL(CREATE_INDEX_DIARY_CREATE_TIME)
            db.execSQL(CREATE_INDEX_MUSIC_TITLE)
            db.execSQL(CREATE_INDEX_MUSIC_ARTIST)
            db.execSQL(CREATE_INDEX_LOG_TIMESTAMP)
            db.execSQL(CREATE_INDEX_LOG_COMPONENT)
            db.execSQL(CREATE_INDEX_LOG_LEVEL)
            Log.d(TAG, "数据库索引创建成功")
            
            Log.d(TAG, "数据库创建完成")
        } catch (e: Exception) {
            Log.e(TAG, "数据库创建失败", e)
            throw e
        }
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "数据库升级：从版本 $oldVersion 到版本 $newVersion")
        
        try {
            when (oldVersion) {
                // 未来版本升级时在这里添加升级逻辑
                // 例如：
                // 1 -> 2: 添加新字段或新表
                // 2 -> 3: 修改表结构等
            }
            Log.d(TAG, "数据库升级完成")
        } catch (e: Exception) {
            Log.e(TAG, "数据库升级失败，将重新创建数据库", e)
            // 如果升级失败，删除所有表并重新创建
            db.execSQL("DROP TABLE IF EXISTS $TABLE_DIARIES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MUSIC")
            onCreate(db)
        }
    }
    
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        Log.d(TAG, "数据库已打开")
        
        // 启用外键约束
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }
    
    /**
     * 获取数据库信息
     */
    fun getDatabaseInfo(): String {
        val db = readableDatabase
        return try {
            val diaryCount = db.rawQuery("SELECT COUNT(*) FROM $TABLE_DIARIES", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
            
            val musicCount = db.rawQuery("SELECT COUNT(*) FROM $TABLE_MUSIC", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
            
            "数据库版本: $DATABASE_VERSION, 日记数量: $diaryCount, 音乐数量: $musicCount"
        } catch (e: Exception) {
            Log.e(TAG, "获取数据库信息失败", e)
            "数据库信息获取失败"
        }
    }
}