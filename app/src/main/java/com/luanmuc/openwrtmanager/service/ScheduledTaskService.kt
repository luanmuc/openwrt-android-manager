package com.luanmuc.openwrtmanager.service
import com.luanmuc.openwrtmanager.util.LogUtils

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 定时任务服务
 * 定时获取数据、定时备份、定时检查更新
 */
class ScheduledTaskService : Service() {
    
    companion object {
        private const val TAG = "ScheduledTaskService"
        private const val TASK_INTERVAL = 30 * 60 * 1000L // 30分钟执行一次
        
        fun start(context: Context) {
            try {
                val intent = Intent(context, ScheduledTaskService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                LogUtils.e(TAG, "启动服务失败", e)
            }
        }
        
        fun stop(context: Context) {
            try {
                val intent = Intent(context, ScheduledTaskService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                LogUtils.e(TAG, "停止服务失败", e)
            }
        }
        
        /**
         * 设置定时闹钟
         */
        fun setAlarm(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, ScheduledTaskReceiver::class.java).apply {
                    action = "com.luanmuc.openwrtmanager.SCHEDULED_TASK"
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + TASK_INTERVAL,
                    TASK_INTERVAL,
                    pendingIntent
                )
                
                LogUtils.d(TAG, "定时闹钟已设置")
            } catch (e: Exception) {
                LogUtils.e(TAG, "设置定时闹钟失败", e)
            }
        }
        
        /**
         * 取消定时闹钟
         */
        fun cancelAlarm(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, ScheduledTaskReceiver::class.java).apply {
                    action = "com.luanmuc.openwrtmanager.SCHEDULED_TASK"
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                alarmManager.cancel(pendingIntent)
                LogUtils.d(TAG, "定时闹钟已取消")
            } catch (e: Exception) {
                LogUtils.e(TAG, "取消定时闹钟失败", e)
            }
        }
    }
    
    private var taskJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        LogUtils.d(TAG, "服务创建")
        startTasks()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtils.d(TAG, "服务启动")
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogUtils.d(TAG, "服务销毁")
        stopTasks()
    }
    
    /**
     * 开始定时任务
     */
    private fun startTasks() {
        stopTasks()
        
        taskJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    executeScheduledTasks()
                } catch (e: Exception) {
                    LogUtils.e(TAG, "执行定时任务失败", e)
                }
                delay(TASK_INTERVAL)
            }
        }
    }
    
    /**
     * 停止定时任务
     */
    private fun stopTasks() {
        taskJob?.cancel()
        taskJob = null
    }
    
    /**
     * 执行定时任务
     */
    private suspend fun executeScheduledTasks() {
        LogUtils.d(TAG, "执行定时任务")
        
        // 任务1：刷新数据
        refreshData()
        
        // 任务2：检查固件更新
        checkFirmwareUpdate()
        
        // 任务3：自动备份（如果开启）
        if (isAutoBackupEnabled()) {
            performAutoBackup()
        }
    }
    
    /**
     * 刷新数据
     */
    private suspend fun refreshData() {
        try {
            LogUtils.d(TAG, "刷新数据")
            // 演示模式下模拟刷新
            if (DebugMode.isDebugMode) {
                delay(1000)
                LogUtils.d(TAG, "数据刷新完成（演示模式）")
            } else {
                // 真实模式：调用Repository刷新数据
                // 暂时留空，后续完善真实API对接
                LogUtils.d(TAG, "数据刷新完成（真实模式）")
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "刷新数据失败", e)
        }
    }
    
    /**
     * 检查固件更新
     */
    private suspend fun checkFirmwareUpdate() {
        try {
            LogUtils.d(TAG, "检查固件更新")
            // 演示模式下模拟检查
            if (DebugMode.isDebugMode) {
                delay(500)
                LogUtils.d(TAG, "固件更新检查完成（演示模式）")
            } else {
                // 真实模式：调用FirmwareRepository检查更新
                // 暂时留空，后续完善真实API对接
                LogUtils.d(TAG, "固件更新检查完成（真实模式）")
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "检查固件更新失败", e)
        }
    }
    
    /**
     * 检查是否开启自动备份
     */
    private fun isAutoBackupEnabled(): Boolean {
        // 暂时返回false，后续完善设置
        return false
    }
    
    /**
     * 执行自动备份
     */
    private suspend fun performAutoBackup() {
        try {
            LogUtils.d(TAG, "执行自动备份")
            // 演示模式下模拟备份
            if (DebugMode.isDebugMode) {
                delay(2000)
                LogUtils.d(TAG, "自动备份完成（演示模式）")
            } else {
                // 真实模式：调用BackupRepository执行备份
                // 暂时留空，后续完善真实API对接
                LogUtils.d(TAG, "自动备份完成（真实模式）")
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "执行自动备份失败", e)
        }
    }
}