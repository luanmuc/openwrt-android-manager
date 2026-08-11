package com.luanmuc.openwrtmanager.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限工具类
 * 提供权限检查和请求功能
 */
object PermissionUtils {
    
    // 常用权限
    const val PERMISSION_INTERNET = Manifest.permission.INTERNET
    const val PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE
    const val PERMISSION_ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE
    const val PERMISSION_CHANGE_WIFI_STATE = Manifest.permission.CHANGE_WIFI_STATE
    const val PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
    const val PERMISSION_FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE
    const val PERMISSION_RECEIVE_BOOT_COMPLETED = Manifest.permission.RECEIVE_BOOT_COMPLETED
    const val PERMISSION_WAKE_LOCK = Manifest.permission.WAKE_LOCK
    const val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    const val PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    const val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    const val PERMISSION_BLUETOOTH = Manifest.permission.BLUETOOTH
    const val PERMISSION_BLUETOOTH_ADMIN = Manifest.permission.BLUETOOTH_ADMIN
    const val PERMISSION_BIOMETRIC = "android.permission.USE_BIOMETRIC"
    
    // 权限请求码
    const val REQUEST_CODE_NOTIFICATION = 1001
    const val REQUEST_CODE_CAMERA = 1002
    const val REQUEST_CODE_STORAGE = 1003
    const val REQUEST_CODE_LOCATION = 1004
    const val REQUEST_CODE_MULTIPLE = 1005
    
    /**
     * 检查是否有权限
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否有多个权限
     */
    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        return permissions.all { hasPermission(context, it) }
    }
    
    /**
     * 检查是否有多个权限（List）
     */
    fun hasPermissions(context: Context, permissions: List<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }
    
    /**
     * 请求单个权限
     */
    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }
    
    /**
     * 请求多个权限
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
    
    /**
     * 请求多个权限（List）
     */
    fun requestPermissions(activity: Activity, permissions: List<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), requestCode)
    }
    
    /**
     * 检查是否需要显示权限说明
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 检查通知权限是否需要请求（Android 13+）
     */
    fun needsNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
    
    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (needsNotificationPermission()) {
            hasPermission(context, PERMISSION_POST_NOTIFICATIONS)
        } else {
            true // Android 12及以下默认有通知权限
        }
    }
    
    /**
     * 请求通知权限
     */
    fun requestNotificationPermission(activity: Activity) {
        if (needsNotificationPermission()) {
            requestPermission(activity, PERMISSION_POST_NOTIFICATIONS, REQUEST_CODE_NOTIFICATION)
        }
    }
    
    /**
     * 检查是否有相机权限
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_CAMERA)
    }
    
    /**
     * 请求相机权限
     */
    fun requestCameraPermission(activity: Activity) {
        requestPermission(activity, PERMISSION_CAMERA, REQUEST_CODE_CAMERA)
    }
    
    /**
     * 检查是否有存储权限
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+使用新的媒体权限
            hasPermission(context, "android.permission.READ_MEDIA_IMAGES") ||
            hasPermission(context, "android.permission.READ_MEDIA_VIDEO") ||
            hasPermission(context, "android.permission.READ_MEDIA_AUDIO")
        } else {
            hasPermission(context, PERMISSION_READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 请求存储权限
     */
    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                activity,
                arrayOf(
                    "android.permission.READ_MEDIA_IMAGES",
                    "android.permission.READ_MEDIA_VIDEO",
                    "android.permission.READ_MEDIA_AUDIO"
                ),
                REQUEST_CODE_STORAGE
            )
        } else {
            requestPermissions(
                activity,
                arrayOf(PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE
            )
        }
    }
    
    /**
     * 检查是否有位置权限
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_ACCESS_FINE_LOCATION) ||
               hasPermission(context, PERMISSION_ACCESS_COARSE_LOCATION)
    }
    
    /**
     * 请求位置权限
     */
    fun requestLocationPermission(activity: Activity) {
        requestPermissions(
            activity,
            arrayOf(PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION),
            REQUEST_CODE_LOCATION
        )
    }
    
    /**
     * 检查是否有网络权限
     */
    fun hasNetworkPermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_INTERNET) &&
               hasPermission(context, PERMISSION_ACCESS_NETWORK_STATE)
    }
    
    /**
     * 检查是否有WiFi权限
     */
    fun hasWifiPermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_ACCESS_WIFI_STATE) &&
               hasPermission(context, PERMISSION_CHANGE_WIFI_STATE)
    }
    
    /**
     * 检查是否有蓝牙权限
     */
    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(context, "android.permission.BLUETOOTH_CONNECT") &&
            hasPermission(context, "android.permission.BLUETOOTH_SCAN")
        } else {
            hasPermission(context, PERMISSION_BLUETOOTH) &&
            hasPermission(context, PERMISSION_BLUETOOTH_ADMIN)
        }
    }
    
    /**
     * 检查是否有前台服务权限
     */
    fun hasForegroundServicePermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_FOREGROUND_SERVICE)
    }
    
    /**
     * 检查是否有开机自启动权限
     */
    fun hasBootCompletedPermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_RECEIVE_BOOT_COMPLETED)
    }
    
    /**
     * 检查是否有唤醒锁权限
     */
    fun hasWakeLockPermission(context: Context): Boolean {
        return hasPermission(context, PERMISSION_WAKE_LOCK)
    }
    
    /**
     * 获取应用已申请的权限列表
     */
    fun getRequestedPermissions(context: Context): List<String> {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
            packageInfo.requestedPermissions?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取应用已授予的权限列表
     */
    fun getGrantedPermissions(context: Context): List<String> {
        return getRequestedPermissions(context).filter { hasPermission(context, it) }
    }
    
    /**
     * 获取应用未授予的权限列表
     */
    fun getDeniedPermissions(context: Context): List<String> {
        return getRequestedPermissions(context).filterNot { hasPermission(context, it) }
    }
    
    /**
     * 检查权限请求结果
     */
    fun isPermissionGranted(
        grantResults: IntArray,
        permissions: Array<String>,
        targetPermission: String
    ): Boolean {
        val index = permissions.indexOf(targetPermission)
        return index >= 0 && grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查所有权限是否都已授予
     */
    fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        return grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }
    
    /**
     * 权限状态枚举
     */
    enum class PermissionStatus {
        GRANTED,      // 已授予
        DENIED,       // 已拒绝
        RATIONALE,    // 需要说明
        UNKNOWN       // 未知
    }
    
    /**
     * 获取权限状态
     */
    fun getPermissionStatus(activity: Activity, permission: String): PermissionStatus {
        return when {
            hasPermission(activity, permission) -> PermissionStatus.GRANTED
            shouldShowRequestPermissionRationale(activity, permission) -> PermissionStatus.RATIONALE
            else -> PermissionStatus.DENIED
        }
    }
    
    /**
     * 检查是否为特殊权限（需要跳转到设置页面）
     */
    fun isSpecialPermission(permission: String): Boolean {
        return when (permission) {
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_SETTINGS",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.MANAGE_EXTERNAL_STORAGE" -> true
            else -> false
        }
    }
}