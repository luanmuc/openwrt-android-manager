package com.luanmuc.openwrtmanager.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 时间工具类
 * 提供时间格式化、计算等功能
 */
object TimeUtils {
    
    // 常用日期格式
    const val FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd"
    const val FORMAT_YEAR_MONTH_DAY_TIME = "yyyy-MM-dd HH:mm:ss"
    const val FORMAT_MONTH_DAY = "MM-dd"
    const val FORMAT_TIME = "HH:mm:ss"
    const val FORMAT_TIME_SHORT = "HH:mm"
    
    /**
     * 格式化时间戳为日期字符串
     */
    fun formatTimestamp(timestamp: Long, pattern: String = FORMAT_YEAR_MONTH_DAY_TIME): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 格式化秒数为可读时间
     */
    fun formatSeconds(seconds: Long): String {
        if (seconds < 0) return "0秒"
        
        val days = TimeUnit.SECONDS.toDays(seconds)
        val hours = TimeUnit.SECONDS.toHours(seconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60
        
        return when {
            days > 0 -> "${days}天${hours}小时"
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟${secs}秒"
            else -> "${secs}秒"
        }
    }
    
    /**
     * 格式化毫秒数为可读时间
     */
    fun formatMilliseconds(milliseconds: Long): String {
        return formatSeconds(milliseconds / 1000)
    }
    
    /**
     * 获取相对时间描述
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            hours < 24 -> "${hours}小时前"
            days < 7 -> "${days}天前"
            days < 30 -> "${days / 7}周前"
            days < 365 -> "${days / 30}个月前"
            else -> "${days / 365}年前"
        }
    }
    
    /**
     * 获取今天开始的时间戳
     */
    fun getTodayStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * 获取本周开始的时间戳
     */
    fun getWeekStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * 获取本月开始的时间戳
     */
    fun getMonthStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * 判断是否是今天
     */
    fun isToday(timestamp: Long): Boolean {
        return timestamp >= getTodayStart() && timestamp < getTodayStart() + 24 * 60 * 60 * 1000
    }
    
    /**
     * 判断是否是本周
     */
    fun isThisWeek(timestamp: Long): Boolean {
        return timestamp >= getWeekStart()
    }
    
    /**
     * 判断是否是本月
     */
    fun isThisMonth(timestamp: Long): Boolean {
        return timestamp >= getMonthStart()
    }
    
    /**
     * 获取当前时间戳
     */
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * 获取当前时间秒数
     */
    fun currentTimeSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }
    
    /**
     * 计算两个时间戳之间的天数差
     */
    fun daysBetween(timestamp1: Long, timestamp2: Long): Long {
        val diff = Math.abs(timestamp2 - timestamp1)
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
    
    /**
     * 计算两个时间戳之间的小时差
     */
    fun hoursBetween(timestamp1: Long, timestamp2: Long): Long {
        val diff = Math.abs(timestamp2 - timestamp1)
        return TimeUnit.MILLISECONDS.toHours(diff)
    }
    
    /**
     * 计算两个时间戳之间的分钟差
     */
    fun minutesBetween(timestamp1: Long, timestamp2: Long): Long {
        val diff = Math.abs(timestamp2 - timestamp1)
        return TimeUnit.MILLISECONDS.toMinutes(diff)
    }
    
    /**
     * 添加天数
     */
    fun addDays(timestamp: Long, days: Int): Long {
        return timestamp + days * 24 * 60 * 60 * 1000L
    }
    
    /**
     * 添加小时
     */
    fun addHours(timestamp: Long, hours: Int): Long {
        return timestamp + hours * 60 * 60 * 1000L
    }
    
    /**
     * 添加分钟
     */
    fun addMinutes(timestamp: Long, minutes: Int): Long {
        return timestamp + minutes * 60 * 1000L
    }
    
    /**
     * 获取当天的小时数
     */
    fun getHourOfDay(timestamp: Long): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(java.util.Calendar.HOUR_OF_DAY)
    }
    
    /**
     * 获取星期几
     * 1 = 周日, 2 = 周一, ..., 7 = 周六
     */
    fun getDayOfWeek(timestamp: Long): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(java.util.Calendar.DAY_OF_WEEK)
    }
    
    /**
     * 判断是否是工作日
     */
    fun isWeekday(timestamp: Long): Boolean {
        val day = getDayOfWeek(timestamp)
        return day != java.util.Calendar.SATURDAY && day != java.util.Calendar.SUNDAY
    }
    
    /**
     * 判断是否是周末
     */
    fun isWeekend(timestamp: Long): Boolean {
        return !isWeekday(timestamp)
    }
}