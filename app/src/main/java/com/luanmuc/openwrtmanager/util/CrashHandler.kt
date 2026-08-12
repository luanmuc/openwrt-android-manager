package com.luanmuc.openwrtmanager.util
import com.luanmuc.openwrtmanager.util.LogUtils

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 崩溃处理工具类
 * 提供崩溃捕获和日志记录功能
 */
object CrashHandler : Thread.UncaughtExceptionHandler {
    
    private const val TAG = "CrashHandler"
    private const val CRASH_LOG_DIR = "crash_logs"
    private const val MAX_LOG_FILES = 10
    
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var context: Context? = null
    private var crashListener: ((String, Throwable) -> Unit)? = null
    
    /**
     * 初始化崩溃处理器
     */
    fun init(context: Context, listener: ((String, Throwable) -> Unit)? = null) {
        this.context = context.applicationContext
        this.crashListener = listener
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        
        LogUtils.d(TAG, "CrashHandler initialized")
    }
    
    /**
     * 处理未捕获的异常
     */
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // 记录崩溃日志
            val crashInfo = collectCrashInfo(thread, throwable)
            saveCrashLog(crashInfo)
            
            // 通知监听器
            crashListener?.invoke(crashInfo, throwable)
            
            LogUtils.e(TAG, "App crashed: ${throwable.message}", throwable)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error handling crash", e)
        } finally {
            // 交给默认处理器处理
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * 收集崩溃信息
     */
    private fun collectCrashInfo(thread: Thread, throwable: Throwable): String {
        val sb = StringBuilder()
        
        // 时间戳
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sb.append("Crash Time: ${timeFormat.format(Date())}\n")
        
        // 线程信息
        sb.append("Thread: ${thread.name} (id=${thread.id}, priority=${thread.priority})\n")
        
        // 应用信息
        context?.let { ctx ->
            sb.append("App Version: ${SystemUtils.getAppVersionName(ctx)} (${SystemUtils.getAppVersionCode(ctx)})\n")
            sb.append("Package: ${SystemUtils.getPackageName(ctx)}\n")
        }
        
        // 设备信息
        sb.append("Device: ${SystemUtils.getDeviceManufacturer()} ${SystemUtils.getDeviceModel()}\n")
        sb.append("Android Version: ${SystemUtils.getAndroidVersion()} (API ${SystemUtils.getSdkVersion()})\n")
        
        // 内存信息
        context?.let { ctx ->
            sb.append("Memory Usage: ${FormatUtils.formatFileSize(SystemUtils.getUsedMemory(ctx))} / ${FormatUtils.formatFileSize(SystemUtils.getTotalMemory(ctx))}\n")
        }
        
        sb.append("\n")
        sb.append("=== Stack Trace ===\n")
        
        // 堆栈信息
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        sb.append(sw.toString())
        
        // 原因链
        var cause = throwable.cause
        while (cause != null) {
            sb.append("\nCaused by: ${cause.javaClass.name}: ${cause.message}\n")
            cause = cause.cause
        }
        
        return sb.toString()
    }
    
    /**
     * 保存崩溃日志到文件
     */
    private fun saveCrashLog(crashInfo: String) {
        try {
            context?.let { ctx ->
                val logDir = File(ctx.filesDir, CRASH_LOG_DIR)
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }
                
                // 生成文件名
                val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "crash_${timeFormat.format(Date())}.log"
                val logFile = File(logDir, fileName)
                
                // 写入文件
                FileWriter(logFile).use { writer ->
                    writer.write(crashInfo)
                }
                
                // 清理旧日志
                cleanOldLogs(logDir)
                
                LogUtils.d(TAG, "Crash log saved: ${logFile.absolutePath}")
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to save crash log", e)
        }
    }
    
    /**
     * 清理旧的崩溃日志
     */
    private fun cleanOldLogs(logDir: File) {
        try {
            val logFiles = logDir.listFiles { file ->
                file.name.endsWith(".log")
            }?.sortedBy { it.lastModified() } ?: return
            
            if (logFiles.size > MAX_LOG_FILES) {
                val filesToDelete = logFiles.size - MAX_LOG_FILES
                for (i in 0 until filesToDelete) {
                    logFiles[i].delete()
                }
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to clean old crash logs", e)
        }
    }
    
    /**
     * 获取所有崩溃日志文件
     */
    fun getCrashLogs(context: Context): List<File> {
        val logDir = File(context.filesDir, CRASH_LOG_DIR)
        return if (logDir.exists()) {
            logDir.listFiles { file ->
                file.name.endsWith(".log")
            }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * 读取崩溃日志内容
     */
    fun readCrashLog(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            "Failed to read crash log: ${e.message}"
        }
    }
    
    /**
     * 删除所有崩溃日志
     */
    fun clearCrashLogs(context: Context) {
        try {
            val logDir = File(context.filesDir, CRASH_LOG_DIR)
            if (logDir.exists()) {
                logDir.deleteRecursively()
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to clear crash logs", e)
        }
    }
    
    /**
     * 获取崩溃日志数量
     */
    fun getCrashLogCount(context: Context): Int {
        return getCrashLogs(context).size
    }
    
    /**
     * 手动记录异常（用于捕获的异常）
     */
    fun logException(tag: String, message: String, throwable: Throwable? = null) {
        LogUtils.e(tag, message, throwable)
        
        // 也可以保存到文件
        try {
            context?.let { ctx ->
                val logDir = File(ctx.filesDir, CRASH_LOG_DIR)
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }
                
                val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "exception_${timeFormat.format(Date())}.log"
                val logFile = File(logDir, fileName)
                
                val sb = StringBuilder()
                sb.append("Time: ${timeFormat.format(Date())}\n")
                sb.append("Tag: $tag\n")
                sb.append("Message: $message\n")
                if (throwable != null) {
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    throwable.printStackTrace(pw)
                    pw.flush()
                    sb.append("\nStack Trace:\n")
                    sb.append(sw.toString())
                }
                
                FileWriter(logFile).use { writer ->
                    writer.write(sb.toString())
                }
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to log exception", e)
        }
    }
}