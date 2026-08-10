package com.luanmuc.openwrtmanager.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 定时任务广播接收器
 * 接收AlarmManager的定时广播，启动定时任务服务
 */
class ScheduledTaskReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ScheduledTaskReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val action = intent?.action
            Log.d(TAG, "收到广播: $action")
            
            when (action) {
                "com.luanmuc.openwrtmanager.SCHEDULED_TASK" -> {
                    // 启动定时任务服务
                    ScheduledTaskService.start(context)
                }
                Intent.ACTION_BOOT_COMPLETED -> {
                    // 开机自启动：设置定时闹钟
                    ScheduledTaskService.setAlarm(context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理广播失败", e)
        }
    }
}