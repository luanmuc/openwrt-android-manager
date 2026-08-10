package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * 版本管理工具类
 * 提供版本号获取、版本比较等功能
 */
object VersionUtils {
    
    /**
     * 获取应用版本名
     */
    fun getVersionName(context: Context): String {
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
    fun getVersionCode(context: Context): Long {
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
     * 比较版本号
     * @return 1 if version1 > version2, -1 if version1 < version2, 0 if equal
     */
    fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1 = v1Parts.getOrElse(i) { 0 }
            val v2 = v2Parts.getOrElse(i) { 0 }
            
            if (v1 > v2) return 1
            if (v1 < v2) return -1
        }
        
        return 0
    }
    
    /**
     * 检查是否需要更新
     */
    fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        return compareVersions(latestVersion, currentVersion) > 0
    }
    
    /**
     * 检查是否是新版本
     */
    fun isNewVersion(currentVersion: String, previousVersion: String): Boolean {
        return compareVersions(currentVersion, previousVersion) > 0
    }
    
    /**
     * 获取主版本号
     */
    fun getMajorVersion(version: String): Int {
        return version.split(".").firstOrNull()?.toIntOrNull() ?: 0
    }
    
    /**
     * 获取次版本号
     */
    fun getMinorVersion(version: String): Int {
        val parts = version.split(".")
        return parts.getOrNull(1)?.toIntOrNull() ?: 0
    }
    
    /**
     * 获取修订版本号
     */
    fun getPatchVersion(version: String): Int {
        val parts = version.split(".")
        return parts.getOrNull(2)?.toIntOrNull() ?: 0
    }
    
    /**
     * 格式化版本号显示
     */
    fun formatVersion(versionName: String, versionCode: Long): String {
        return "v$versionName ($versionCode)"
    }
    
    /**
     * 检查是否是预发布版本
     */
    fun isPreRelease(version: String): Boolean {
        val lowerVersion = version.lowercase()
        return lowerVersion.contains("alpha") || 
               lowerVersion.contains("beta") || 
               lowerVersion.contains("rc") ||
               lowerVersion.contains("dev") ||
               lowerVersion.contains("preview")
    }
    
    /**
     * 获取Android SDK版本
     */
    fun getAndroidSdkVersion(): Int {
        return Build.VERSION.SDK_INT
    }
    
    /**
     * 获取Android版本名
     */
    fun getAndroidVersionName(): String {
        return Build.VERSION.RELEASE
    }
    
    /**
     * 检查是否满足最低版本要求
     */
    fun meetsMinimumVersion(currentVersion: String, minimumVersion: String): Boolean {
        return compareVersions(currentVersion, minimumVersion) >= 0
    }
    
    /**
     * 版本号范围检查
     */
    fun isVersionInRange(version: String, minVersion: String, maxVersion: String): Boolean {
        return compareVersions(version, minVersion) >= 0 && compareVersions(version, maxVersion) <= 0
    }
}