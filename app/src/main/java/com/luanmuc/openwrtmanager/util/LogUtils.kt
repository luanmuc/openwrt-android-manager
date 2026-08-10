package com.luanmuc.openwrtmanager.util

import android.util.Log

/**
 * 日志工具类
 * 提供日志脱敏、统一日志格式等功能
 */
object LogUtils {
    
    private const val TAG = "OpenWrtManager"
    private const val MAX_LOG_LENGTH = 4000
    
    // 是否启用调试日志
    var isDebugEnabled = true
    
    // 敏感关键词列表
    private val sensitiveKeywords = listOf(
        "password", "passwd", "pwd",
        "token", "access_token", "refresh_token",
        "secret", "api_key", "apikey",
        "cookie", "session",
        "authorization", "auth",
        "private_key", "public_key"
    )
    
    /**
     * 调试日志
     */
    fun d(tag: String = TAG, message: String) {
        if (isDebugEnabled) {
            val sanitized = sanitizeMessage(message)
            logChunked(Log.DEBUG, tag, sanitized)
        }
    }
    
    /**
     * 信息日志
     */
    fun i(tag: String = TAG, message: String) {
        val sanitized = sanitizeMessage(message)
        logChunked(Log.INFO, tag, sanitized)
    }
    
    /**
     * 警告日志
     */
    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) {
        val sanitized = sanitizeMessage(message)
        if (throwable != null) {
            Log.w(tag, sanitized, throwable)
        } else {
            logChunked(Log.WARN, tag, sanitized)
        }
    }
    
    /**
     * 错误日志
     */
    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        val sanitized = sanitizeMessage(message)
        if (throwable != null) {
            Log.e(tag, sanitized, throwable)
        } else {
            logChunked(Log.ERROR, tag, sanitized)
        }
    }
    
    /**
     * 分块打印长日志
     */
    private fun logChunked(priority: Int, tag: String, message: String) {
        if (message.length <= MAX_LOG_LENGTH) {
            Log.println(priority, tag, message)
            return
        }
        
        var i = 0
        while (i < message.length) {
            val end = minOf(i + MAX_LOG_LENGTH, message.length)
            Log.println(priority, tag, message.substring(i, end))
            i = end
        }
    }
    
    /**
     * 脱敏日志消息
     */
    fun sanitizeMessage(message: String): String {
        var result = message
        
        // 脱敏密码
        result = result.replace(
            Regex("\"(password|passwd|pwd)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE),
            "\"$1\":\"***\""
        )
        
        // 脱敏Token
        result = result.replace(
            Regex("\"(token|access_token|refresh_token|secret|api_key|apikey)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE),
            "\"$1\":\"***\""
        )
        
        // 脱敏Cookie
        result = result.replace(
            Regex("\"(cookie|session)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE),
            "\"$1\":\"***\""
        )
        
        // 脱敏Authorization头
        result = result.replace(
            Regex("(Authorization|auth)\\s*:\\s*[^\n]*", RegexOption.IGNORE_CASE),
            "$1: ***"
        )
        
        // 脱敏URL中的密码
        result = result.replace(
            Regex("(https?://[^:]+):[^@]+@"),
            "$1:***@"
        )
        
        return result
    }
    
    /**
     * 脱敏密码
     */
    fun maskPassword(password: String): String {
        if (password.isEmpty()) return ""
        return "*".repeat(password.length.coerceAtMost(8))
    }
    
    /**
     * 脱敏手机号
     */
    fun maskPhone(phone: String): String {
        if (phone.length < 7) return phone
        return phone.substring(0, 3) + "****" + phone.substring(7)
    }
    
    /**
     * 脱敏邮箱
     */
    fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email
        val username = email.substring(0, atIndex)
        val domain = email.substring(atIndex)
        return if (username.length <= 2) {
            username + "***" + domain
        } else {
            username.substring(0, 2) + "***" + domain
        }
    }
    
    /**
     * 脱敏IP地址
     */
    fun maskIp(ip: String): String {
        val parts = ip.split(".")
        if (parts.size != 4) return ip
        return "${parts[0]}.${parts[1]}.***.***"
    }
    
    /**
     * 脱敏MAC地址
     */
    fun maskMac(mac: String): String {
        val parts = mac.split(":")
        if (parts.size != 6) return mac
        return "${parts[0]}:${parts[1]}:${parts[2]}:**:**:**"
    }
}