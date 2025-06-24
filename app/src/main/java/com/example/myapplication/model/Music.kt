package com.example.myapplication.model

/**
 * 音乐数据模型
 */
data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumId: Long = 0
) {
    /**
     * 格式化播放时长
     */
    fun getFormattedDuration(): String {
        val minutes = duration / 1000 / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}