package com.example.myapplication.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.myapplication.model.Diary
import com.example.myapplication.receiver.AlarmReceiver

/**
 * 提醒管理器
 */
class ReminderManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ReminderManager"
    }
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * 设置日记提醒
     */
    fun setReminder(diary: Diary) {
        Log.d(TAG, "[MyDiaryApp] 开始设置提醒 - 日记ID: ${diary.id}, 标题: ${diary.title}")
        Log.d(TAG, "[MyDiaryApp] 提醒状态: isReminder=${diary.isReminder}, reminderTime=${diary.reminderTime}")
        Log.d(TAG, "[MyDiaryApp] 当前时间: ${System.currentTimeMillis()}, 提醒时间: ${diary.reminderTime}")
        
        if (!diary.isReminder || diary.reminderTime <= 0) {
            Log.w(TAG, "[MyDiaryApp] 日记未设置提醒或提醒时间无效 - isReminder: ${diary.isReminder}, reminderTime: ${diary.reminderTime}")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        if (diary.reminderTime <= currentTime) {
            Log.w(TAG, "[MyDiaryApp] 提醒时间已过期 - 提醒时间: ${diary.reminderTime}, 当前时间: ${currentTime}")
            return
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_DIARY_TITLE, diary.title)
            putExtra(AlarmReceiver.EXTRA_DIARY_CONTENT, diary.content)
            putExtra(AlarmReceiver.EXTRA_DIARY_ID, diary.id)
        }
        Log.d(TAG, "[MyDiaryApp] 创建Intent完成 - 目标: AlarmReceiver")
        
        val requestCode = diary.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(TAG, "[MyDiaryApp] 创建PendingIntent完成 - requestCode: $requestCode")
        
        try {
            Log.d(TAG, "[MyDiaryApp] 开始设置AlarmManager - Android版本: ${Build.VERSION.SDK_INT}")
            
            // 检查是否有精确闹钟权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canScheduleExact = alarmManager.canScheduleExactAlarms()
                Log.d(TAG, "[MyDiaryApp] 精确闹钟权限检查: $canScheduleExact")
                
                if (canScheduleExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        diary.reminderTime,
                        pendingIntent
                    )
                    Log.i(TAG, "[MyDiaryApp] 设置精确提醒成功: ${diary.title} at ${diary.reminderTime} (${java.util.Date(diary.reminderTime)})")
                } else {
                    // 如果没有精确闹钟权限，使用普通闹钟
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        diary.reminderTime,
                        pendingIntent
                    )
                    Log.w(TAG, "[MyDiaryApp] 无精确闹钟权限，设置普通提醒: ${diary.title} at ${diary.reminderTime} (${java.util.Date(diary.reminderTime)})")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    diary.reminderTime,
                    pendingIntent
                )
                Log.i(TAG, "[MyDiaryApp] 设置提醒成功(旧版本): ${diary.title} at ${diary.reminderTime} (${java.util.Date(diary.reminderTime)})")
            }
            
            Log.d(TAG, "[MyDiaryApp] AlarmManager设置完成，等待触发时间")
        } catch (e: Exception) {
            Log.e(TAG, "[MyDiaryApp] 设置提醒失败 - 日记: ${diary.title}, 错误: ${e.message}", e)
        }
    }
    
    /**
     * 取消日记提醒
     */
    fun cancelReminder(diaryId: String) {
        Log.d(TAG, "[MyDiaryApp] 开始取消提醒 - 日记ID: $diaryId")
        
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = diaryId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        Log.d(TAG, "[MyDiaryApp] 创建取消用PendingIntent - requestCode: $requestCode")
        
        try {
            alarmManager.cancel(pendingIntent)
            Log.i(TAG, "[MyDiaryApp] 取消提醒成功: $diaryId")
        } catch (e: Exception) {
            Log.e(TAG, "[MyDiaryApp] 取消提醒失败 - 日记ID: $diaryId, 错误: ${e.message}", e)
        }
    }
    
    /**
     * 更新日记提醒
     */
    fun updateReminder(oldDiary: Diary?, newDiary: Diary) {
        // 先取消旧的提醒
        oldDiary?.let {
            if (it.isReminder) {
                cancelReminder(it.id)
            }
        }
        
        // 设置新的提醒
        if (newDiary.isReminder) {
            setReminder(newDiary)
        }
    }
}