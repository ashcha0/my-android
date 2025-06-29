package com.example.myapplication.model

import java.io.Serializable
import java.util.*

/**
 * 日记数据模型
 */
data class Diary(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: String, // 格式: yyyy-MM-dd
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis(),
    val mood: String = "普通", // 心情：开心、普通、难过等
    val weather: String = "", // 天气信息
    val tags: List<String> = emptyList(), // 标签
    val isReminder: Boolean = false, // 是否设置提醒
    val reminderTime: Long = 0L // 提醒时间
) : Serializable

/**
 * 查看模式枚举
 */
enum class ViewMode {
    ALL,     // 全部
    TODAY,   // 今天
    WEEK,    // 本周
    MONTH    // 本月
}