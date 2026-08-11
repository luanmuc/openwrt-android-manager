package com.luanmuc.openwrtmanager.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.io.File

/**
 * 系统工具类
 * 提供系统相关的工具函数
 */
object SystemUtils {
    
    /**
     * 获取应用版本名
     */
    fun getAppVersionName(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    /**
     * 获取应用版本号
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1
        }
    }
    
    /**
     * 获取应用包名
     */
    fun getPackageName(context: Context): String {
        return context.packageName
    }
    
    /**
     * 检查应用是否安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * 获取Android SDK版本
     */
    fun getSdkVersion(): Int {
        return Build.VERSION.SDK_INT
    }
    
    /**
     * 获取Android版本名
     */
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }
    
    /**
     * 获取设备型号
     */
    fun getDeviceModel(): String {
        return Build.MODEL
    }
    
    /**
     * 获取设备制造商
     */
    fun getDeviceManufacturer(): String {
        return Build.MANUFACTURER
    }
    
    /**
     * 获取设备品牌
     */
    fun getDeviceBrand(): String {
        return Build.BRAND
    }
    
    /**
     * 获取设备名称
     */
    fun getDeviceName(): String {
        return Build.DEVICE
    }
    
    /**
     * 获取产品名称
     */
    fun getProductName(): String {
        return Build.PRODUCT
    }
    
    /**
     * 获取硬件名称
     */
    fun getHardwareName(): String {
        return Build.HARDWARE
    }
    
    /**
     * 获取Android ID
     */
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }
    
    /**
     * 检查是否有网络连接
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * 检查是否是WiFi连接
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * 检查是否是移动数据连接
     */
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    /**
     * 检查是否是以太网连接
     */
    fun isEthernetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    /**
     * 获取可用内存大小
     */
    fun getAvailableMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 0L
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
    
    /**
     * 获取总内存大小
     */
    fun getTotalMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 0L
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }
    
    /**
     * 获取已用内存大小
     */
    fun getUsedMemory(context: Context): Long {
        return getTotalMemory(context) - getAvailableMemory(context)
    }
    
    /**
     * 获取内存使用率
     */
    fun getMemoryUsagePercent(context: Context): Float {
        val total = getTotalMemory(context)
        if (total == 0L) return 0f
        
        val used = getUsedMemory(context)
        return (used.toFloat() / total.toFloat()) * 100
    }
    
    /**
     * 检查是否是低内存状态
     */
    fun isLowMemory(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }
    
    /**
     * 获取应用进程ID
     */
    fun getPid(): Int {
        return Process.myPid()
    }
    
    /**
     * 获取应用进程名
     */
    fun getProcessName(context: Context): String {
        val pid = getPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return ""
        for (process in activityManager.runningAppProcesses) {
            if (process.pid == pid) {
                return process.processName
            }
        }
        return ""
    }
    
    /**
     * 检查是否是主进程
     */
    fun isMainProcess(context: Context): Boolean {
        return getProcessName(context) == context.packageName
    }
    
    /**
     * 获取应用缓存大小
     */
    fun getAppCacheSize(context: Context): Long {
        var size = 0L
        
        // 内部缓存
        size += FileUtils.getDirectorySize(context.cacheDir.absolutePath)
        
        // 外部缓存
        context.externalCacheDir?.let { externalCache ->
            size += FileUtils.getDirectorySize(externalCache.absolutePath)
        }
        
        return size
    }
    
    /**
     * 清除应用缓存
     */
    fun clearAppCache(context: Context): Long {
        return FileUtils.clearCache(context)
    }
    
    /**
     * 重启应用
     */
    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        
        // 杀死当前进程
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }
    
    /**
     * 打开应用设置页面
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = android.net.Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }
    
    /**
     * 打开WiFi设置
     */
    fun openWifiSettings(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    /**
     * 打开网络设置
     */
    fun openNetworkSettings(context: Context) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}