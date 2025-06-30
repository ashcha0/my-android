package com.example.myapplication.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.myapplication.model.Music
import java.io.File

/**
 * 音乐数据访问对象
 * 提供音乐数据的增删改查操作
 */
class MusicDao(private val dbHelper: DatabaseHelper) {
    
    companion object {
        private const val TAG = "[MyDiaryApp] MusicDao"
    }
    
    /**
     * 插入或更新音乐
     */
    fun saveMusic(music: Music): Boolean {
        Log.d(TAG, "开始保存音乐: ${music.title}")
        
        val db = dbHelper.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_MUSIC_ID, music.id)
                put(DatabaseHelper.COLUMN_MUSIC_TITLE, music.title)
                put(DatabaseHelper.COLUMN_MUSIC_ARTIST, music.artist)
                put(DatabaseHelper.COLUMN_MUSIC_ALBUM, music.album)
                put(DatabaseHelper.COLUMN_MUSIC_DURATION, music.duration)
                put(DatabaseHelper.COLUMN_MUSIC_PATH, music.path)
                put(DatabaseHelper.COLUMN_MUSIC_ALBUM_ID, music.albumId)
                put(DatabaseHelper.COLUMN_MUSIC_ADD_TIME, System.currentTimeMillis())
            }
            
            val result = db.insertWithOnConflict(
                DatabaseHelper.TABLE_MUSIC,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            
            val success = result != -1L
            if (success) {
                Log.d(TAG, "音乐保存成功: ${music.title}")
            } else {
                Log.e(TAG, "音乐保存失败: ${music.title}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "保存音乐时发生异常: ${music.title}", e)
            false
        }
    }
    
    /**
     * 批量保存音乐列表
     */
    fun saveMusicList(musicList: List<Music>): Boolean {
        Log.d(TAG, "开始批量保存音乐列表，共${musicList.size}首")
        
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        
        return try {
            var successCount = 0
            musicList.forEach { music ->
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_MUSIC_ID, music.id)
                    put(DatabaseHelper.COLUMN_MUSIC_TITLE, music.title)
                    put(DatabaseHelper.COLUMN_MUSIC_ARTIST, music.artist)
                    put(DatabaseHelper.COLUMN_MUSIC_ALBUM, music.album)
                    put(DatabaseHelper.COLUMN_MUSIC_DURATION, music.duration)
                    put(DatabaseHelper.COLUMN_MUSIC_PATH, music.path)
                    put(DatabaseHelper.COLUMN_MUSIC_ALBUM_ID, music.albumId)
                    put(DatabaseHelper.COLUMN_MUSIC_ADD_TIME, System.currentTimeMillis())
                }
                
                val result = db.insertWithOnConflict(
                    DatabaseHelper.TABLE_MUSIC,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                
                if (result != -1L) {
                    successCount++
                }
            }
            
            db.setTransactionSuccessful()
            Log.d(TAG, "批量保存音乐列表成功，成功保存${successCount}首，共${musicList.size}首")
            successCount == musicList.size
        } catch (e: Exception) {
            Log.e(TAG, "批量保存音乐列表时发生异常", e)
            false
        } finally {
            db.endTransaction()
        }
    }
    
    /**
     * 根据ID删除音乐
     */
    fun deleteMusic(musicId: Long): Boolean {
        Log.d(TAG, "开始删除音乐: $musicId")
        
        val db = dbHelper.writableDatabase
        return try {
            val deletedRows = db.delete(
                DatabaseHelper.TABLE_MUSIC,
                "${DatabaseHelper.COLUMN_MUSIC_ID} = ?",
                arrayOf(musicId.toString())
            )
            
            val success = deletedRows > 0
            if (success) {
                Log.d(TAG, "音乐删除成功: $musicId")
            } else {
                Log.w(TAG, "音乐删除失败，可能不存在: $musicId")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "删除音乐时发生异常: $musicId", e)
            false
        }
    }
    
    /**
     * 根据路径删除音乐
     */
    fun deleteMusicByPath(path: String): Boolean {
        Log.d(TAG, "根据路径删除音乐: $path")
        
        val db = dbHelper.writableDatabase
        return try {
            val deletedRows = db.delete(
                DatabaseHelper.TABLE_MUSIC,
                "${DatabaseHelper.COLUMN_MUSIC_PATH} = ?",
                arrayOf(path)
            )
            
            val success = deletedRows > 0
            if (success) {
                Log.d(TAG, "根据路径删除音乐成功: $path")
            } else {
                Log.w(TAG, "根据路径删除音乐失败，可能不存在: $path")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "根据路径删除音乐时发生异常: $path", e)
            false
        }
    }
    
    /**
     * 根据ID获取音乐
     */
    fun getMusicById(musicId: Long): Music? {
        Log.d(TAG, "根据ID获取音乐: $musicId")
        
        val db = dbHelper.readableDatabase
        return try {
            db.query(
                DatabaseHelper.TABLE_MUSIC,
                null,
                "${DatabaseHelper.COLUMN_MUSIC_ID} = ?",
                arrayOf(musicId.toString()),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val music = cursorToMusic(cursor)
                    Log.d(TAG, "找到音乐: ${music.title}")
                    music
                } else {
                    Log.w(TAG, "未找到音乐: $musicId")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "根据ID获取音乐时发生异常: $musicId", e)
            null
        }
    }
    
    /**
     * 获取所有音乐，按添加时间倒序排列
     */
    fun getAllMusic(): List<Music> {
        Log.d(TAG, "获取所有音乐")
        
        val db = dbHelper.readableDatabase
        val musicList = mutableListOf<Music>()
        
        return try {
            db.query(
                DatabaseHelper.TABLE_MUSIC,
                null,
                null,
                null,
                null,
                null,
                "${DatabaseHelper.COLUMN_MUSIC_ADD_TIME} DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val music = cursorToMusic(cursor)
                    // 验证文件是否仍然存在
                    val file = File(music.path)
                    if (file.exists()) {
                        musicList.add(music)
                    } else {
                        Log.w(TAG, "音乐文件不存在，跳过: ${music.title} - ${music.path}")
                        // 可以选择在这里删除数据库中的记录
                        // deleteMusicByPath(music.path)
                    }
                }
            }
            
            Log.d(TAG, "获取所有音乐成功，共${musicList.size}首")
            musicList
        } catch (e: Exception) {
            Log.e(TAG, "获取所有音乐时发生异常", e)
            emptyList()
        }
    }
    
    /**
     * 搜索音乐（根据标题、艺术家、专辑）
     */
    fun searchMusic(keyword: String): List<Music> {
        if (keyword.isBlank()) {
            return getAllMusic()
        }
        
        Log.d(TAG, "搜索音乐，关键词: $keyword")
        
        val db = dbHelper.readableDatabase
        val musicList = mutableListOf<Music>()
        
        return try {
            val searchPattern = "%$keyword%"
            db.query(
                DatabaseHelper.TABLE_MUSIC,
                null,
                "${DatabaseHelper.COLUMN_MUSIC_TITLE} LIKE ? OR ${DatabaseHelper.COLUMN_MUSIC_ARTIST} LIKE ? OR ${DatabaseHelper.COLUMN_MUSIC_ALBUM} LIKE ?",
                arrayOf(searchPattern, searchPattern, searchPattern),
                null,
                null,
                "${DatabaseHelper.COLUMN_MUSIC_ADD_TIME} DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val music = cursorToMusic(cursor)
                    // 验证文件是否仍然存在
                    val file = File(music.path)
                    if (file.exists()) {
                        musicList.add(music)
                    } else {
                        Log.w(TAG, "音乐文件不存在，跳过: ${music.title} - ${music.path}")
                    }
                }
            }
            
            Log.d(TAG, "搜索音乐成功，关键词: $keyword，共${musicList.size}首")
            musicList
        } catch (e: Exception) {
            Log.e(TAG, "搜索音乐时发生异常，关键词: $keyword", e)
            emptyList()
        }
    }
    
    /**
     * 根据艺术家获取音乐
     */
    fun getMusicByArtist(artist: String): List<Music> {
        Log.d(TAG, "根据艺术家获取音乐: $artist")
        
        val db = dbHelper.readableDatabase
        val musicList = mutableListOf<Music>()
        
        return try {
            db.query(
                DatabaseHelper.TABLE_MUSIC,
                null,
                "${DatabaseHelper.COLUMN_MUSIC_ARTIST} = ?",
                arrayOf(artist),
                null,
                null,
                "${DatabaseHelper.COLUMN_MUSIC_ADD_TIME} DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    musicList.add(cursorToMusic(cursor))
                }
            }
            
            Log.d(TAG, "根据艺术家获取音乐成功: $artist，共${musicList.size}首")
            musicList
        } catch (e: Exception) {
            Log.e(TAG, "根据艺术家获取音乐时发生异常: $artist", e)
            emptyList()
        }
    }
    
    /**
     * 获取音乐总数
     */
    fun getMusicCount(): Int {
        val db = dbHelper.readableDatabase
        return try {
            db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_MUSIC}", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    Log.d(TAG, "音乐总数: $count")
                    count
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取音乐总数时发生异常", e)
            0
        }
    }
    
    /**
     * 清空所有音乐记录
     */
    fun clearAllMusic(): Boolean {
        Log.d(TAG, "开始清空所有音乐记录")
        
        val db = dbHelper.writableDatabase
        return try {
            val deletedRows = db.delete(DatabaseHelper.TABLE_MUSIC, null, null)
            Log.d(TAG, "清空所有音乐记录成功，删除了${deletedRows}条记录")
            true
        } catch (e: Exception) {
            Log.e(TAG, "清空所有音乐记录时发生异常", e)
            false
        }
    }
    
    /**
     * 检查音乐是否已存在（根据路径）
     */
    fun isMusicExists(path: String): Boolean {
        val db = dbHelper.readableDatabase
        return try {
            db.query(
                DatabaseHelper.TABLE_MUSIC,
                arrayOf(DatabaseHelper.COLUMN_MUSIC_ID),
                "${DatabaseHelper.COLUMN_MUSIC_PATH} = ?",
                arrayOf(path),
                null,
                null,
                null
            ).use { cursor ->
                val exists = cursor.count > 0
                Log.d(TAG, "检查音乐是否存在: $path，结果: $exists")
                exists
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查音乐是否存在时发生异常: $path", e)
            false
        }
    }
    
    /**
     * 清理无效的音乐记录（文件不存在的记录）
     */
    fun cleanInvalidMusic(): Int {
        Log.d(TAG, "开始清理无效的音乐记录")
        
        val allMusic = getAllMusic()
        var cleanedCount = 0
        
        allMusic.forEach { music ->
            val file = File(music.path)
            if (!file.exists()) {
                if (deleteMusicByPath(music.path)) {
                    cleanedCount++
                    Log.d(TAG, "清理无效音乐记录: ${music.title} - ${music.path}")
                }
            }
        }
        
        Log.d(TAG, "清理无效音乐记录完成，清理了${cleanedCount}条记录")
        return cleanedCount
    }
    
    /**
     * 将Cursor转换为Music对象
     */
    private fun cursorToMusic(cursor: Cursor): Music {
        return Music(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_TITLE)),
            artist = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_ARTIST)),
            album = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_ALBUM)),
            duration = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_DURATION)),
            path = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_PATH)),
            albumId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MUSIC_ALBUM_ID))
        )
    }
}