package com.luanmuc.openwrtmanager.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 主题管理器
 * 支持浅色、深色、跟随系统三种模式
 */
class ThemeManager(private val context: Context) {

    val themeMode: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)

    suspend fun setThemeMode(mode: ThemeMode) {
        // 暂时使用默认主题，后续完善
    }

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * 主题模式枚举
 */
enum class ThemeMode {
    LIGHT,      // 浅色模式
    DARK,       // 深色模式
    SYSTEM      // 跟随系统
}
