package com.example.myapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.DiaryActivity
import com.example.myapplication.R

/**
 * 通知帮助类
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "diary_reminder_channel"
        private const val CHANNEL_NAME = "日记提醒"
        private const val CHANNEL_DESCRIPTION = "日记提醒通知"
        const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * 创建通知渠道（Android 8.0及以上需要）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.util.Log.d("NotificationHelper", "[MyDiaryApp] 开始创建通知渠道 - Android ${Build.VERSION.SDK_INT}")
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            android.util.Log.i("NotificationHelper", "[MyDiaryApp] 通知渠道创建完成 - ID: $CHANNEL_ID, 名称: $CHANNEL_NAME")
        } else {
            android.util.Log.d("NotificationHelper", "[MyDiaryApp] Android版本 ${Build.VERSION.SDK_INT} 无需创建通知渠道")
        }
    }
    
    /**
     * 显示日记提醒通知
     */
    fun showReminderNotification(title: String, content: String) {
        android.util.Log.d("NotificationHelper", "[MyDiaryApp] 开始创建通知 - 标题: $title")
        
        // 创建点击通知后的意图
        val intent = Intent(context, DiaryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        android.util.Log.d("NotificationHelper", "[MyDiaryApp] PendingIntent创建完成")
        
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("日记提醒")
            .setContentText("该写日记了：$title")
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 1000))
            .build()
        
        android.util.Log.d("NotificationHelper", "[MyDiaryApp] 通知构建完成 - 渠道ID: $CHANNEL_ID, 通知ID: $NOTIFICATION_ID")
        
        // 显示通知
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            android.util.Log.d("NotificationHelper", "[MyDiaryApp] 通知管理器获取完成，开始显示通知")
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            android.util.Log.i("NotificationHelper", "[MyDiaryApp] 通知显示成功 - ID: $NOTIFICATION_ID, 标题: $title")
        } catch (e: SecurityException) {
            // 处理权限不足的情况
            android.util.Log.e("NotificationHelper", "[MyDiaryApp] 通知权限不足，显示失败", e)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "[MyDiaryApp] 通知显示异常 - 错误: ${e.message}", e)
        }
    }
    
    /**
     * 检查通知权限
     */
    fun hasNotificationPermission(): Boolean {
        android.util.Log.d("NotificationHelper", "[MyDiaryApp] 开始检查通知权限 - Android版本: ${Build.VERSION.SDK_INT}")
        
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            android.util.Log.d("NotificationHelper", "[MyDiaryApp] Android 13+ 通知权限检查结果: $enabled")
            enabled
        } else {
            android.util.Log.d("NotificationHelper", "[MyDiaryApp] Android 13以下版本，默认有通知权限")
            true
        }
        
        android.util.Log.i("NotificationHelper", "[MyDiaryApp] 通知权限最终结果: $hasPermission")
        return hasPermission
    }
}