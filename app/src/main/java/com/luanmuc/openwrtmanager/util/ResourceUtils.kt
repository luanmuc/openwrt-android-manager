package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * 资源工具类
 * 提供资源访问的便捷方法
 */
object ResourceUtils {
    
    /**
     * 获取字符串资源
     */
    fun getString(context: Context, @StringRes resId: Int): String {
        return context.getString(resId)
    }
    
    /**
     * 获取带格式的字符串资源
     */
    fun getString(context: Context, @StringRes resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
    
    /**
     * 获取颜色资源
     */
    fun getColor(context: Context, @ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
    
    /**
     * 获取Drawable资源
     */
    fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }
    
    /**
     * 获取尺寸资源（像素）
     */
    fun getDimensionPixelSize(context: Context, resId: Int): Int {
        return context.resources.getDimensionPixelSize(resId)
    }
    
    /**
     * 获取尺寸资源（浮点）
     */
    fun getDimension(context: Context, resId: Int): Float {
        return context.resources.getDimension(resId)
    }
    
    /**
     * 获取整数资源
     */
    fun getInteger(context: Context, resId: Int): Int {
        return context.resources.getInteger(resId)
    }
    
    /**
     * 获取布尔资源
     */
    fun getBoolean(context: Context, resId: Int): Boolean {
        return context.resources.getBoolean(resId)
    }
    
    /**
     * 获取字符串数组资源
     */
    fun getStringArray(context: Context, resId: Int): Array<String> {
        return context.resources.getStringArray(resId)
    }
    
    /**
     * 获取整数数组资源
     */
    fun getIntArray(context: Context, resId: Int): IntArray {
        return context.resources.getIntArray(resId)
    }
    
    /**
     * 获取颜色数组资源
     */
    fun getColorArray(context: Context, resId: Int): IntArray {
        return context.resources.getIntArray(resId)
    }
    
    /**
     * 获取资源名称
     */
    fun getResourceName(context: Context, resId: Int): String {
        return context.resources.getResourceName(resId)
    }
    
    /**
     * 获取资源条目名称
     */
    fun getResourceEntryName(context: Context, resId: Int): String {
        return context.resources.getResourceEntryName(resId)
    }
    
    /**
     * 获取资源类型名称
     */
    fun getResourceTypeName(context: Context, resId: Int): String {
        return context.resources.getResourceTypeName(resId)
    }
    
    /**
     * 根据名称获取资源ID
     */
    fun getIdentifier(context: Context, name: String, defType: String): Int {
        return context.resources.getIdentifier(name, defType, context.packageName)
    }
    
    /**
     * 检查资源是否存在
     */
    fun resourceExists(context: Context, name: String, defType: String): Boolean {
        return getIdentifier(context, name, defType) != 0
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
     * 获取屏幕缩放密度
     */
    fun getScaledDensity(context: Context): Float {
        return context.resources.displayMetrics.scaledDensity
    }
    
    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
    
    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
    
    /**
     * 检查是否为平板
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }
    
    /**
     * 检查是否为大屏
     */
    fun isLargeScreen(context: Context): Boolean {
        return context.resources.configuration.screenLayout and 
               android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK >= 
               android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    /**
     * 检查是否为横屏
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == 
               android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }
    
    /**
     * 检查是否为竖屏
     */
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == 
               android.content.res.Configuration.ORIENTATION_PORTRAIT
    }
    
    /**
     * 获取当前语言
     */
    fun getCurrentLanguage(context: Context): String {
        return context.resources.configuration.locales[0].language
    }
    
    /**
     * 获取当前国家
     */
    fun getCurrentCountry(context: Context): String {
        return context.resources.configuration.locales[0].country
    }
    
    /**
     * 获取当前Locale
     */
    fun getCurrentLocale(context: Context): java.util.Locale {
        return context.resources.configuration.locales[0]
    }
    
    /**
     * 检查是否为RTL布局
     */
    fun isRtl(context: Context): Boolean {
        return context.resources.configuration.layoutDirection == 
               android.view.View.LAYOUT_DIRECTION_RTL
    }
    
    /**
     * 获取主题属性
     */
    fun getThemeAttribute(context: Context, attrId: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrId))
        val value = typedArray.getResourceId(0, 0)
        typedArray.recycle()
        return value
    }
    
    /**
     * 获取主题颜色属性
     */
    fun getThemeColor(context: Context, attrId: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
    
    /**
     * 获取主题尺寸属性
     */
    fun getThemeDimension(context: Context, attrId: Int): Float {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrId))
        val dimension = typedArray.getDimension(0, 0f)
        typedArray.recycle()
        return dimension
    }
    
    /**
     * 获取主题布尔属性
     */
    fun getThemeBoolean(context: Context, attrId: Int): Boolean {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrId))
        val value = typedArray.getBoolean(0, false)
        typedArray.recycle()
        return value
    }
    
    /**
     * 获取应用名称
     */
    fun getAppName(context: Context): String {
        return context.applicationInfo.loadLabel(context.packageManager).toString()
    }
    
    /**
     * 获取应用版本名
     */
    fun getAppVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 获取应用版本号
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取应用包名
     */
    fun getPackageName(context: Context): String {
        return context.packageName
    }
    
    /**
     * 获取应用图标
     */
    fun getAppIcon(context: Context): Drawable? {
        return context.applicationInfo.loadIcon(context.packageManager)
    }
}