package com.luanmuc.openwrtmanager.util

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.Locale

/**
 * 字符串工具类
 * 提供字符串处理、编码、加密等功能
 */
object StringUtils {
    
    /**
     * 检查字符串是否为空
     */
    fun isEmpty(str: String?): Boolean {
        return str.isNullOrEmpty()
    }
    
    /**
     * 检查字符串是否为空或空白
     */
    fun isBlank(str: String?): Boolean {
        return str.isNullOrBlank()
    }
    
    /**
     * 检查字符串是否不为空
     */
    fun isNotEmpty(str: String?): Boolean {
        return !str.isNullOrEmpty()
    }
    
    /**
     * 检查字符串是否不为空或空白
     */
    fun isNotBlank(str: String?): Boolean {
        return !str.isNullOrBlank()
    }
    
    /**
     * 安全获取字符串长度
     */
    fun safeLength(str: String?): Int {
        return str?.length ?: 0
    }
    
    /**
     * 截断字符串
     */
    fun truncate(str: String, maxLength: Int, suffix: String = "..."): String {
        return if (str.length <= maxLength) {
            str
        } else {
            str.substring(0, maxLength - suffix.length) + suffix
        }
    }
    
    /**
     * 首字母大写
     */
    fun capitalize(str: String): String {
        return if (str.isEmpty()) {
            str
        } else {
            str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
        }
    }
    
    /**
     * 首字母小写
     */
    fun decapitalize(str: String): String {
        return if (str.isEmpty()) {
            str
        } else {
            str.substring(0, 1).lowercase(Locale.getDefault()) + str.substring(1)
        }
    }
    
    /**
     * 转换为驼峰命名
     */
    fun toCamelCase(str: String, separator: Char = '_'): String {
        val parts = str.split(separator)
        if (parts.size <= 1) return str
        
        return parts.first().lowercase(Locale.getDefault()) + 
               parts.drop(1).joinToString("") { it.replaceFirstChar { char -> 
                   if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() 
               } }
    }
    
    /**
     * 转换为下划线命名
     */
    fun toSnakeCase(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])"), "$1_$2")
                  .lowercase(Locale.getDefault())
    }
    
    /**
     * 反转字符串
     */
    fun reverse(str: String): String {
        return str.reversed()
    }
    
    /**
     * 去除空格
     */
    fun removeSpaces(str: String): String {
        return str.replace(" ", "")
    }
    
    /**
     * 去除换行符
     */
    fun removeNewlines(str: String): String {
        return str.replace("\n", "").replace("\r", "")
    }
    
    /**
     * 统计字符出现次数
     */
    fun countOccurrences(str: String, char: Char): Int {
        return str.count { it == char }
    }
    
    /**
     * 统计子字符串出现次数
     */
    fun countOccurrences(str: String, subStr: String): Int {
        if (subStr.isEmpty()) return 0
        
        var count = 0
        var index = 0
        
        while (index < str.length) {
            val found = str.indexOf(subStr, index)
            if (found == -1) break
            count++
            index = found + subStr.length
        }
        
        return count
    }
    
    /**
     * 检查是否包含子字符串（忽略大小写）
     */
    fun containsIgnoreCase(str: String, subStr: String): Boolean {
        return str.contains(subStr, ignoreCase = true)
    }
    
    /**
     * 检查是否以指定字符串开头（忽略大小写）
     */
    fun startsWithIgnoreCase(str: String, prefix: String): Boolean {
        return str.startsWith(prefix, ignoreCase = true)
    }
    
    /**
     * 检查是否以指定字符串结尾（忽略大小写）
     */
    fun endsWithIgnoreCase(str: String, suffix: String): Boolean {
        return str.endsWith(suffix, ignoreCase = true)
    }
    
    /**
     * URL编码
     */
    fun urlEncode(str: String): String {
        return try {
            URLEncoder.encode(str, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * URL解码
     */
    fun urlDecode(str: String): String {
        return try {
            URLDecoder.decode(str, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * Base64编码
     */
    fun base64Encode(str: String): String {
        return try {
            Base64.getEncoder().encodeToString(str.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * Base64解码
     */
    fun base64Decode(str: String): String {
        return try {
            String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * MD5加密
     */
    fun md5(str: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(str.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * SHA-256加密
     */
    fun sha256(str: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(str.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * 隐藏中间部分（用于敏感信息显示）
     */
    fun maskMiddle(str: String, keepStart: Int = 3, keepEnd: Int = 3, maskChar: Char = '*'): String {
        if (str.length <= keepStart + keepEnd) return str
        
        val maskLength = str.length - keepStart - keepEnd
        val mask = maskChar.toString().repeat(maskLength)
        
        return str.substring(0, keepStart) + mask + str.substring(str.length - keepEnd)
    }
    
    /**
     * 格式化数字（添加千位分隔符）
     */
    fun formatNumber(number: Long): String {
        return String.format(Locale.getDefault(), "%,d", number)
    }
    
    /**
     * 格式化数字（添加千位分隔符，带小数）
     */
    fun formatNumber(number: Double, decimalPlaces: Int = 2): String {
        return String.format(Locale.getDefault(), "%,.${decimalPlaces}f", number)
    }
    
    /**
     * 填充字符串到指定长度
     */
    fun padStart(str: String, length: Int, padChar: Char = '0'): String {
        return str.padStart(length, padChar)
    }
    
    /**
     * 填充字符串到指定长度（末尾）
     */
    fun padEnd(str: String, length: Int, padChar: Char = '0'): String {
        return str.padEnd(length, padChar)
    }
    
    /**
     * 检查是否是数字字符串
     */
    fun isNumeric(str: String): Boolean {
        return str.toLongOrNull() != null || str.toDoubleOrNull() != null
    }
    
    /**
     * 检查是否是整数
     */
    fun isInteger(str: String): Boolean {
        return str.toIntOrNull() != null || str.toLongOrNull() != null
    }
    
    /**
     * 检查是否只包含字母
     */
    fun isAlpha(str: String): Boolean {
        return str.all { it.isLetter() }
    }
    
    /**
     * 检查是否只包含字母和数字
     */
    fun isAlphanumeric(str: String): Boolean {
        return str.all { it.isLetterOrDigit() }
    }
    
    /**
     * 重复字符串
     */
    fun repeat(str: String, count: Int): String {
        return str.repeat(count)
    }
    
    /**
     * 安全转换为整数
     */
    fun toIntOrNull(str: String?): Int? {
        return str?.toIntOrNull()
    }
    
    /**
     * 安全转换为长整数
     */
    fun toLongOrNull(str: String?): Long? {
        return str?.toLongOrNull()
    }
    
    /**
     * 安全转换为浮点数
     */
    fun toFloatOrNull(str: String?): Float? {
        return str?.toFloatOrNull()
    }
    
    /**
     * 安全转换为双精度浮点数
     */
    fun toDoubleOrNull(str: String?): Double? {
        return str?.toDoubleOrNull()
    }
    
    /**
     * 比较两个字符串是否相等（忽略大小写）
     */
    fun equalsIgnoreCase(str1: String?, str2: String?): Boolean {
        return str1.equals(str2, ignoreCase = true)
    }
    
    /**
     * 比较两个字符串（null安全）
     */
    fun nullSafeCompare(str1: String?, str2: String?): Int {
        return when {
            str1 == null && str2 == null -> 0
            str1 == null -> -1
            str2 == null -> 1
            else -> str1.compareTo(str2)
        }
    }
}