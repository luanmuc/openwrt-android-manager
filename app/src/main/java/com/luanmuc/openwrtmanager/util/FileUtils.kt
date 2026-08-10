package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.DecimalFormat

/**
 * 文件工具类
 * 提供文件操作、大小计算等功能
 */
object FileUtils {
    
    /**
     * 获取应用文件目录
     */
    fun getAppFilesDir(context: Context): File {
        return context.filesDir
    }
    
    /**
     * 获取应用缓存目录
     */
    fun getAppCacheDir(context: Context): File {
        return context.cacheDir
    }
    
    /**
     * 获取外部存储目录
     */
    fun getExternalStorageDir(): File {
        return Environment.getExternalStorageDirectory()
    }
    
    /**
     * 获取应用外部文件目录
     */
    fun getExternalFilesDir(context: Context, type: String? = null): File? {
        return context.getExternalFilesDir(type)
    }
    
    /**
     * 获取应用外部缓存目录
     */
    fun getExternalCacheDir(context: Context): File? {
        return context.externalCacheDir
    }
    
    /**
     * 检查文件是否存在
     */
    fun fileExists(path: String): Boolean {
        return try {
            File(path).exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否是文件
     */
    fun isFile(path: String): Boolean {
        return try {
            File(path).isFile
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否是目录
     */
    fun isDirectory(path: String): Boolean {
        return try {
            File(path).isDirectory
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取文件大小
     */
    fun getFileSize(path: String): Long {
        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.length()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取目录大小（递归）
     */
    fun getDirectorySize(path: String): Long {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return 0
            }
            
            var size = 0L
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file.absolutePath)
                } else {
                    file.length()
                }
            }
            size
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceAtMost(units.size - 1)
        
        val value = size / Math.pow(1024.0, index.toDouble())
        val format = DecimalFormat("#,##0.##")
        
        return "${format.format(value)} ${units[index]}"
    }
    
    /**
     * 创建目录
     */
    fun createDirectory(path: String): Boolean {
        return try {
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 删除目录（递归）
     */
    fun deleteDirectory(path: String): Boolean {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return false
            }
            
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteDirectory(file.absolutePath)
                } else {
                    file.delete()
                }
            }
            
            dir.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 清理缓存目录
     */
    fun clearCache(context: Context): Long {
        var freedSize = 0L
        
        // 清理内部缓存
        freedSize += getDirectorySize(context.cacheDir.absolutePath)
        deleteDirectory(context.cacheDir.absolutePath)
        
        // 清理外部缓存
        context.externalCacheDir?.let { externalCache ->
            freedSize += getDirectorySize(externalCache.absolutePath)
            deleteDirectory(externalCache.absolutePath)
        }
        
        return freedSize
    }
    
    /**
     * 获取文件扩展名
     */
    fun getFileExtension(path: String): String {
        val file = File(path)
        val name = file.name
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0 && dotIndex < name.length - 1) {
            name.substring(dotIndex + 1).lowercase()
        } else {
            ""
        }
    }
    
    /**
     * 获取文件名（不含扩展名）
     */
    fun getFileNameWithoutExtension(path: String): String {
        val file = File(path)
        val name = file.name
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) {
            name.substring(0, dotIndex)
        } else {
            name
        }
    }
    
    /**
     * 检查外部存储是否可用
     */
    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    /**
     * 检查外部存储是否可写
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    /**
     * 检查外部存储是否可读
     */
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }
    
    /**
     * 获取可用空间
     */
    fun getAvailableSpace(path: String): Long {
        return try {
            val file = File(path)
            file.freeSpace
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取总空间
     */
    fun getTotalSpace(path: String): Long {
        return try {
            val file = File(path)
            file.totalSpace
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取已用空间
     */
    fun getUsedSpace(path: String): Long {
        return getTotalSpace(path) - getAvailableSpace(path)
    }
    
    /**
     * 计算存储空间使用率
     */
    fun getStorageUsagePercent(path: String): Float {
        val total = getTotalSpace(path)
        if (total == 0L) return 0f
        
        val used = getUsedSpace(path)
        return (used.toFloat() / total.toFloat()) * 100
    }
    
    /**
     * 列出目录中的文件
     */
    fun listFiles(path: String): Array<File>? {
        return try {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 统计目录中的文件数量
     */
    fun countFiles(path: String): Int {
        return listFiles(path)?.size ?: 0
    }
}