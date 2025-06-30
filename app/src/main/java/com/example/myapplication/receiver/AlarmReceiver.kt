package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.utils.NotificationHelper

/**
 * 闹钟接收器，用于处理日记提醒
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        const val EXTRA_DIARY_TITLE = "extra_diary_title"
        const val EXTRA_DIARY_CONTENT = "extra_diary_content"
        const val EXTRA_DIARY_ID = "extra_diary_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val currentTime = System.currentTimeMillis()
        Log.i(TAG, "[MyDiaryApp] ========== 收到提醒广播 ========== 时间: ${java.util.Date(currentTime)}")
        Log.d(TAG, "[MyDiaryApp] Intent Action: ${intent.action}")
        Log.d(TAG, "[MyDiaryApp] Intent Extras: ${intent.extras?.keySet()?.joinToString()}")
        
        val diaryTitle = intent.getStringExtra(EXTRA_DIARY_TITLE) ?: "日记提醒"
        val diaryContent = intent.getStringExtra(EXTRA_DIARY_CONTENT) ?: "该写日记了"
        val diaryId = intent.getStringExtra(EXTRA_DIARY_ID) ?: ""
        
        Log.i(TAG, "[MyDiaryApp] 提醒详情 - ID: $diaryId, 标题: $diaryTitle")
        Log.d(TAG, "[MyDiaryApp] 提醒内容: $diaryContent")
        
        // 显示通知
        val notificationHelper = NotificationHelper(context)
        val hasPermission = notificationHelper.hasNotificationPermission()
        Log.d(TAG, "[MyDiaryApp] 通知权限检查: $hasPermission")
        
        if (hasPermission) {
            Log.d(TAG, "[MyDiaryApp] 开始显示通知")
            notificationHelper.showReminderNotification(diaryTitle, diaryContent)
            Log.i(TAG, "[MyDiaryApp] 通知显示完成")
        } else {
            Log.w(TAG, "[MyDiaryApp] 没有通知权限，无法显示通知")
        }
        
        Log.d(TAG, "[MyDiaryApp] ========== 广播处理完成 ==========")
    }
}