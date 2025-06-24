package com.example.myapplication.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.example.myapplication.model.Music

/**
 * 音乐扫描工具类
 */
class MusicScanner(private val context: Context) {
    
    companion object {
        private const val TAG = "MusicScanner"
    }

    /**
     * 扫描设备中的音乐文件
     */
    fun scanMusicFiles(): List<Music> {
        val musicList = mutableListOf<Music>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
            )
            
            cursor?.let {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val album = it.getString(albumColumn) ?: "Unknown Album"
                    val duration = it.getLong(durationColumn)
                    val path = it.getString(dataColumn) ?: ""
                    val albumId = it.getLong(albumIdColumn)
                    
                    val music = Music(id, title, artist, album, duration, path, albumId)
                    musicList.add(music)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning music files: ${e.message}")
        } finally {
            cursor?.close()
        }
        
        return musicList
    }
}