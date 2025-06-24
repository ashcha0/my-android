package com.example.myapplication

import android.net.Uri

data class MediaFile(
    val id: Long,
    val title: String,
    val artist: String?,
    val duration: Long,
    val uri: Uri,
    val mimeType: String,
    val size: Long
) {
    val isVideo: Boolean
        get() = mimeType.startsWith("video/")
    
    val isAudio: Boolean
        get() = mimeType.startsWith("audio/")
    
    fun getDurationString(): String {
        val minutes = duration / 1000 / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun getSizeString(): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            else -> "${size / 1024 / 1024}MB"
        }
    }
}