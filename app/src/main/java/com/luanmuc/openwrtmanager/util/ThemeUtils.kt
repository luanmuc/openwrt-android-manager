package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

/**
 * 主题工具类
 * 提供主题相关的工具函数
 */
object ThemeUtils {
    
    // 主题模式枚举
    enum class ThemeMode {
        LIGHT,      // 浅色模式
        DARK,       // 深色模式
        SYSTEM      // 跟随系统
    }
    
    /**
     * 检查是否是深色模式
     */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * 检查是否是浅色模式
     */
    fun isLightMode(context: Context): Boolean {
        return !isDarkMode(context)
    }
    
    /**
     * 设置应用主题模式
     */
    fun setThemeMode(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
    
    /**
     * 获取当前主题模式
     */
    fun getThemeMode(): ThemeMode {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> ThemeMode.LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }
    
    /**
     * 切换主题模式（浅色/深色）
     */
    fun toggleTheme(context: Context): ThemeMode {
        val currentMode = if (isDarkMode(context)) {
            ThemeMode.LIGHT
        } else {
            ThemeMode.DARK
        }
        setThemeMode(currentMode)
        return currentMode
    }
    
    /**
     * 检查是否支持系统深色模式
     */
    fun supportsSystemDarkMode(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
    
    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    
    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    
    /**
     * 检查是否有导航栏
     */
    fun hasNavigationBar(context: Context): Boolean {
        val resourceId = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (resourceId > 0) {
            context.resources.getBoolean(resourceId)
        } else {
            false
        }
    }
    
    /**
     * 获取屏幕宽度（像素）
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }
    
    /**
     * 获取屏幕高度（像素）
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }
    
    /**
     * 获取屏幕密度
     */
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }
    
    /**
     * 获取屏幕密度DPI
     */
    fun getScreenDensityDpi(context: Context): Int {
        return context.resources.displayMetrics.densityDpi
    }
    
    /**
     * dp转px
     */
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
    
    /**
     * px转dp
     */
    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
    
    /**
     * sp转px
     */
    fun spToPx(context: Context, sp: Float): Float {
        return sp * context.resources.displayMetrics.scaledDensity
    }
    
    /**
     * px转sp
     */
    fun pxToSp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }
    
    /**
     * 检查是否是平板设备
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    /**
     * 检查是否是横屏
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    
    /**
     * 检查是否是竖屏
     */
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }
    
    /**
     * 获取最小宽度（dp）
     */
    fun getSmallestScreenWidthDp(context: Context): Int {
        return context.resources.configuration.smallestScreenWidthDp
    }
    
    /**
     * 检查是否是大屏幕
     */
    fun isLargeScreen(context: Context): Boolean {
        return getSmallestScreenWidthDp(context) >= 600
    }
    
    /**
     * 检查是否是超大屏幕
     */
    fun isXLargeScreen(context: Context): Boolean {
        return getSmallestScreenWidthDp(context) >= 720
    }
}