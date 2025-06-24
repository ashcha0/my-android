package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.model.Music
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * 音乐列表存储管理类
 * 负责音乐列表的持久化存储和文件管理
 */
class MusicStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "MusicStorage"
        private const val MUSIC_LIST_FILE = "music_list.json"
        private const val MUSIC_FILES_DIR = "music_files"
    }
    
    private val gson = Gson()
    private val musicListFile = File(context.filesDir, MUSIC_LIST_FILE)
    private val musicFilesDir = File(context.filesDir, MUSIC_FILES_DIR)
    
    init {
        // 确保音乐文件目录存在
        if (!musicFilesDir.exists()) {
            val created = musicFilesDir.mkdirs()
            Log.d(TAG, "创建音乐文件目录: $created")
        }
    }
    
    /**
     * 保存音乐列表到本地存储
     */
    fun saveMusicList(musicList: List<Music>) {
        try {
            Log.d(TAG, "开始保存音乐列表，共 ${musicList.size} 首音乐")
            val json = gson.toJson(musicList)
            musicListFile.writeText(json)
            Log.d(TAG, "音乐列表保存成功")
        } catch (e: Exception) {
            Log.e(TAG, "保存音乐列表失败", e)
        }
    }
    
    /**
     * 从本地存储加载音乐列表
     */
    fun loadMusicList(): List<Music> {
        return try {
            if (!musicListFile.exists()) {
                Log.d(TAG, "音乐列表文件不存在，返回空列表")
                return emptyList()
            }
            
            val json = musicListFile.readText()
            val type = object : TypeToken<List<Music>>() {}.type
            val musicList: List<Music> = gson.fromJson(json, type) ?: emptyList()
            
            Log.d(TAG, "加载音乐列表成功，共 ${musicList.size} 首音乐")
            
            // 验证文件是否仍然存在
            val validMusicList = musicList.filter { music ->
                val file = File(music.path)
                val exists = file.exists()
                if (!exists) {
                    Log.w(TAG, "音乐文件不存在，已移除: ${music.title} - ${music.path}")
                }
                exists
            }
            
            if (validMusicList.size != musicList.size) {
                Log.d(TAG, "移除了 ${musicList.size - validMusicList.size} 个无效的音乐文件")
                // 保存更新后的列表
                saveMusicList(validMusicList)
            }
            
            validMusicList
        } catch (e: Exception) {
            Log.e(TAG, "加载音乐列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 将音乐文件复制到应用私有目录
     * @param uri 原始文件URI
     * @param fileName 文件名
     * @return 复制后的文件路径，失败返回null
     */
    fun copyMusicToPrivateDirectory(uri: Uri, fileName: String): String? {
        return copyMusicFileToPrivateDir(uri, fileName)
    }
    
    /**
     * 将音乐文件复制到应用私有目录（内部实现）
     * @param uri 原始文件URI
     * @param fileName 文件名
     * @return 复制后的文件路径，失败返回null
     */
    private fun copyMusicFileToPrivateDir(uri: Uri, fileName: String): String? {
        return try {
            Log.d(TAG, "开始复制音乐文件到私有目录: $fileName")
            
            // 确保文件名是安全的
            val safeFileName = sanitizeFileName(fileName)
            val targetFile = File(musicFilesDir, safeFileName)
            
            // 如果文件已存在，生成新的文件名
            val finalFile = if (targetFile.exists()) {
                generateUniqueFileName(musicFilesDir, safeFileName)
            } else {
                targetFile
            }
            
            Log.d(TAG, "目标文件路径: ${finalFile.absolutePath}")
            
            // 复制文件
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(finalFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            Log.d(TAG, "文件复制成功: ${finalFile.absolutePath}")
            finalFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "复制音乐文件失败", e)
            null
        }
    }
    
    /**
     * 删除音乐文件
     */
    fun deleteMusicFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists() && file.parentFile == musicFilesDir) {
                val deleted = file.delete()
                Log.d(TAG, "删除音乐文件: $filePath, 结果: $deleted")
                deleted
            } else {
                Log.w(TAG, "文件不存在或不在私有目录中: $filePath")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除音乐文件失败: $filePath", e)
            false
        }
    }
    
    /**
     * 清理所有音乐文件和列表
     */
    fun clearAllMusic() {
        try {
            Log.d(TAG, "开始清理所有音乐文件和列表")
            
            // 删除音乐列表文件
            if (musicListFile.exists()) {
                musicListFile.delete()
                Log.d(TAG, "删除音乐列表文件")
            }
            
            // 删除所有音乐文件
            if (musicFilesDir.exists()) {
                musicFilesDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        file.delete()
                        Log.d(TAG, "删除音乐文件: ${file.name}")
                    }
                }
            }
            
            Log.d(TAG, "清理完成")
        } catch (e: Exception) {
            Log.e(TAG, "清理音乐文件失败", e)
        }
    }
    
    /**
     * 获取音乐文件目录大小
     */
    fun getMusicDirectorySize(): Long {
        return try {
            var size = 0L
            musicFilesDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
            Log.d(TAG, "音乐目录大小: ${size / 1024 / 1024} MB")
            size
        } catch (e: Exception) {
            Log.e(TAG, "计算目录大小失败", e)
            0L
        }
    }
    
    /**
     * 清理文件名，移除不安全字符
     */
    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._\u4e00-\u9fa5-]"), "_")
    }
    
    /**
     * 生成唯一的文件名
     */
    private fun generateUniqueFileName(directory: File, fileName: String): File {
        val nameWithoutExt = fileName.substringBeforeLast('.')
        val extension = fileName.substringAfterLast('.', "")
        
        var counter = 1
        var newFile: File
        
        do {
            val newFileName = if (extension.isNotEmpty()) {
                "${nameWithoutExt}_$counter.$extension"
            } else {
                "${nameWithoutExt}_$counter"
            }
            newFile = File(directory, newFileName)
            counter++
        } while (newFile.exists())
        
        return newFile
    }
}