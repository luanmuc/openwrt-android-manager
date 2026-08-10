package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * 偏好设置工具类
 * 提供SharedPreferences的便捷访问
 */
object PreferencesUtils {
    
    private const val PREFS_NAME = "openwrt_manager_prefs"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 获取字符串值
     */
    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return getPreferences(context).getString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * 设置字符串值
     */
    fun putString(context: Context, key: String, value: String) {
        getPreferences(context).edit {
            putString(key, value)
        }
    }
    
    /**
     * 获取整数值
     */
    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getPreferences(context).getInt(key, defaultValue)
    }
    
    /**
     * 设置整数值
     */
    fun putInt(context: Context, key: String, value: Int) {
        getPreferences(context).edit {
            putInt(key, value)
        }
    }
    
    /**
     * 获取长整数
     */
    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        return getPreferences(context).getLong(key, defaultValue)
    }
    
    /**
     * 设置长整数
     */
    fun putLong(context: Context, key: String, value: Long) {
        getPreferences(context).edit {
            putLong(key, value)
        }
    }
    
    /**
     * 获取浮点值
     */
    fun getFloat(context: Context, key: String, defaultValue: Float = 0f): Float {
        return getPreferences(context).getFloat(key, defaultValue)
    }
    
    /**
     * 设置浮点值
     */
    fun putFloat(context: Context, key: String, value: Float) {
        getPreferences(context).edit {
            putFloat(key, value)
        }
    }
    
    /**
     * 获取布尔值
     */
    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getPreferences(context).getBoolean(key, defaultValue)
    }
    
    /**
     * 设置布尔值
     */
    fun putBoolean(context: Context, key: String, value: Boolean) {
        getPreferences(context).edit {
            putBoolean(key, value)
        }
    }
    
    /**
     * 检查是否包含某个键
     */
    fun contains(context: Context, key: String): Boolean {
        return getPreferences(context).contains(key)
    }
    
    /**
     * 移除某个键
     */
    fun remove(context: Context, key: String) {
        getPreferences(context).edit {
            remove(key)
        }
    }
    
    /**
     * 清除所有数据
     */
    fun clear(context: Context) {
        getPreferences(context).edit {
            clear()
        }
    }
    
    /**
     * 获取所有键值对
     */
    fun getAll(context: Context): Map<String, *> {
        return getPreferences(context).all
    }
    
    /**
     * 注册监听器
     */
    fun registerOnSharedPreferenceChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        getPreferences(context).registerOnSharedPreferenceChangeListener(listener)
    }
    
    /**
     * 注销监听器
     */
    fun unregisterOnSharedPreferenceChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        getPreferences(context).unregisterOnSharedPreferenceChangeListener(listener)
    }
    
    /**
     * 自增计数器
     */
    fun increment(context: Context, key: String, defaultValue: Int = 0): Int {
        val current = getInt(context, key, defaultValue)
        val newValue = current + 1
        putInt(context, key, newValue)
        return newValue
    }
    
    /**
     * 自减计数器
     */
    fun decrement(context: Context, key: String, defaultValue: Int = 0): Int {
        val current = getInt(context, key, defaultValue)
        val newValue = (current - 1).coerceAtLeast(0)
        putInt(context, key, newValue)
        return newValue
    }
    
    /**
     * 切换布尔值
     */
    fun toggle(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val current = getBoolean(context, key, defaultValue)
        val newValue = !current
        putBoolean(context, key, newValue)
        return newValue
    }
    
    /**
     * 获取字符串集合
     */
    fun getStringSet(context: Context, key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return getPreferences(context).getStringSet(key, defaultValue) ?: defaultValue
    }
    
    /**
     * 设置字符串集合
     */
    fun putStringSet(context: Context, key: String, value: Set<String>) {
        getPreferences(context).edit {
            putStringSet(key, value)
        }
    }
    
    /**
     * 添加到字符串集合
     */
    fun addToStringSet(context: Context, key: String, value: String) {
        val set = getStringSet(context, key).toMutableSet()
        set.add(value)
        putStringSet(context, key, set)
    }
    
    /**
     * 从字符串集合移除
     */
    fun removeFromStringSet(context: Context, key: String, value: String) {
        val set = getStringSet(context, key).toMutableSet()
        set.remove(value)
        putStringSet(context, key, set)
    }
    
    /**
     * 检查字符串集合是否包含
     */
    fun stringSetContains(context: Context, key: String, value: String): Boolean {
        return getStringSet(context, key).contains(value)
    }
}