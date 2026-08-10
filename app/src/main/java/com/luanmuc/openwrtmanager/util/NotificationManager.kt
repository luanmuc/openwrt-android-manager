package com.luanmuc.openwrtmanager.util

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.luanmuc.openwrtmanager.MainActivity
import com.luanmuc.openwrtmanager.R

/**
 * 通知管理器
 * 负责管理所有APP通知
 */
object AppNotificationManager {
    
    // 通知渠道ID
    const val CHANNEL_ROUTER_STATUS = "router_status"
    const val CHANNEL_FIRMWARE = "firmware_update"
    const val CHANNEL_NETWORK = "network_alert"
    const val CHANNEL_DEVICE = "device_event"
    
    // 通知ID
    private const val NOTIFICATION_OFFLINE = 1001
    private const val NOTIFICATION_FIRMWARE = 1002
    private const val NOTIFICATION_NEW_DEVICE = 1003
    private const val NOTIFICATION_NETWORK_ERROR = 1004
    
    private var context: Context? = null
    private var initialized = false
    
    /**
     * 初始化通知管理器
     */
    fun init(context: Context) {
        if (initialized) return
        this.context = context.applicationContext
        initialized = true
        
        createNotificationChannels()
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ctx = context ?: return
            val nm = ctx.getSystemService(AndroidNotificationManager::class.java)
            
            // 路由器状态渠道
            val statusChannel = NotificationChannel(
                CHANNEL_ROUTER_STATUS,
                "路由器状态",
                AndroidNotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "路由器在线/离线状态通知"
            }
            
            // 固件更新渠道
            val firmwareChannel = NotificationChannel(
                CHANNEL_FIRMWARE,
                "固件更新",
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "新固件版本发布通知"
            }
            
            // 网络告警渠道
            val networkChannel = NotificationChannel(
                CHANNEL_NETWORK,
                "网络告警",
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "网络异常、断网等告警通知"
            }
            
            // 设备事件渠道
            val deviceChannel = NotificationChannel(
                CHANNEL_DEVICE,
                "设备事件",
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "新设备上线、设备上下线通知"
            }
            
            nm.createNotificationChannels(listOf(
                statusChannel, firmwareChannel, networkChannel, deviceChannel
            ))
        }
    }
    
    /**
     * 显示路由器离线通知
     */
    fun showRouterOfflineNotification(routerName: String) {
        val ctx = context ?: return
        
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ROUTER_STATUS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("路由器已离线")
            .setContentText("$routerName 已断开连接")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_OFFLINE, notification)
        } catch (e: SecurityException) {
            // 通知权限未授予
        }
    }
    
    /**
     * 显示路由器在线通知
     */
    fun showRouterOnlineNotification(routerName: String) {
        val ctx = context ?: return
        
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ROUTER_STATUS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("路由器已恢复在线")
            .setContentText("$routerName 已重新连接")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_OFFLINE, notification)
        } catch (e: SecurityException) {
            // 通知权限未授予
        }
    }
    
    /**
     * 显示固件更新通知
     */
    fun showFirmwareUpdateNotification(version: String, size: String) {
        val ctx = context ?: return
        
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate", "firmware")
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(ctx, CHANNEL_FIRMWARE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("新固件可用")
            .setContentText("版本 $version ($size)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("发现新固件版本 $version，大小 $size。点击查看更新详情。"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_FIRMWARE, notification)
        } catch (e: SecurityException) {
            // 通知权限未授予
        }
    }
    
    /**
     * 显示新设备上线通知
     */
    fun showNewDeviceNotification(deviceName: String, deviceIp: String) {
        val ctx = context ?: return
        
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate", "devices")
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(ctx, CHANNEL_DEVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("新设备上线")
            .setContentText("$deviceName ($deviceIp) 已连接")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_NEW_DEVICE, notification)
        } catch (e: SecurityException) {
            // 通知权限未授予
        }
    }
    
    /**
     * 显示网络异常通知
     */
    fun showNetworkErrorNotification(message: String) {
        val ctx = context ?: return
        
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate", "diagnostic")
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(ctx, CHANNEL_NETWORK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("网络异常")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_NETWORK_ERROR, notification)
        } catch (e: SecurityException) {
            // 通知权限未授予
        }
    }
    
    /**
     * 取消所有通知
     */
    fun cancelAll() {
        val ctx = context ?: return
        try {
            NotificationManagerCompat.from(ctx).cancelAll()
        } catch (e: Exception) {
            // 忽略
        }
    }
}
