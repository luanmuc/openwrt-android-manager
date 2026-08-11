package com.luanmuc.openwrtmanager.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.roundToInt

/**
 * 颜色工具类
 * 提供颜色处理和转换功能
 */
object ColorUtils {
    
    /**
     * 根据背景颜色计算合适的文字颜色（黑或白）
     */
    fun getContrastColor(background: Color): Color {
        return if (background.luminance() > 0.5) {
            Color.Black
        } else {
            Color.White
        }
    }
    
    /**
     * 调整颜色亮度
     * @param color 原始颜色
     * @param factor 亮度因子，0.0-1.0为变暗，>1.0为变亮
     */
    fun adjustBrightness(color: Color, factor: Float): Color {
        val red = (color.red * factor).coerceIn(0f, 1f)
        val green = (color.green * factor).coerceIn(0f, 1f)
        val blue = (color.blue * factor).coerceIn(0f, 1f)
        return Color(red, green, blue, color.alpha)
    }
    
    /**
     * 颜色变亮
     */
    fun lighten(color: Color, amount: Float = 0.1f): Color {
        return adjustBrightness(color, 1f + amount)
    }
    
    /**
     * 颜色变暗
     */
    fun darken(color: Color, amount: Float = 0.1f): Color {
        return adjustBrightness(color, 1f - amount)
    }
    
    /**
     * 颜色混合
     */
    fun blend(color1: Color, color2: Color, ratio: Float = 0.5f): Color {
        val r = ratio.coerceIn(0f, 1f)
        return Color(
            red = color1.red * (1 - r) + color2.red * r,
            green = color1.green * (1 - r) + color2.green * r,
            blue = color1.blue * (1 - r) + color2.blue * r,
            alpha = color1.alpha * (1 - r) + color2.alpha * r
        )
    }
    
    /**
     * 设置颜色透明度
     */
    fun withAlpha(color: Color, alpha: Float): Color {
        return color.copy(alpha = alpha.coerceIn(0f, 1f))
    }
    
    /**
     * Color转十六进制字符串
     */
    fun toHex(color: Color, includeAlpha: Boolean = true): String {
        val alpha = (color.alpha * 255).roundToInt()
        val red = (color.red * 255).roundToInt()
        val green = (color.green * 255).roundToInt()
        val blue = (color.blue * 255).roundToInt()
        
        return if (includeAlpha) {
            String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
        } else {
            String.format("#%02X%02X%02X", red, green, blue)
        }
    }
    
    /**
     * 十六进制字符串转Color
     */
    fun fromHex(hex: String): Color? {
        return try {
            val cleanHex = hex.removePrefix("#")
            when (cleanHex.length) {
                6 -> {
                    val red = cleanHex.substring(0, 2).toInt(16) / 255f
                    val green = cleanHex.substring(2, 4).toInt(16) / 255f
                    val blue = cleanHex.substring(4, 6).toInt(16) / 255f
                    Color(red, green, blue)
                }
                8 -> {
                    val alpha = cleanHex.substring(0, 2).toInt(16) / 255f
                    val red = cleanHex.substring(2, 4).toInt(16) / 255f
                    val green = cleanHex.substring(4, 6).toInt(16) / 255f
                    val blue = cleanHex.substring(6, 8).toInt(16) / 255f
                    Color(red, green, blue, alpha)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 计算两种颜色之间的距离
     */
    fun colorDistance(color1: Color, color2: Color): Double {
        val dr = color1.red - color2.red
        val dg = color1.green - color2.green
        val db = color1.blue - color2.blue
        return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
    }
    
    /**
     * 检查颜色是否为浅色
     */
    fun isLight(color: Color): Boolean {
        return color.luminance() > 0.5
    }
    
    /**
     * 检查颜色是否为深色
     */
    fun isDark(color: Color): Boolean {
        return color.luminance() <= 0.5
    }
    
    /**
     * 生成随机颜色
     */
    fun randomColor(alpha: Float = 1f): Color {
        return Color(
            red = kotlin.random.Random.nextFloat(),
            green = kotlin.random.Random.nextFloat(),
            blue = kotlin.random.Random.nextFloat(),
            alpha = alpha
        )
    }
    
    /**
     * 根据数值生成渐变色（从绿到红）
     * 适用于显示使用率、温度等指标
     */
    fun getHeatColor(value: Float, min: Float = 0f, max: Float = 100f): Color {
        val normalized = ((value - min) / (max - min)).coerceIn(0f, 1f)
        return when {
            normalized < 0.5f -> {
                // 绿色到黄色
                blend(Color(0xFF00B578), Color(0xFFFF7D00), normalized * 2)
            }
            else -> {
                // 黄色到红色
                blend(Color(0xFFFF7D00), Color(0xFFF53F3F), (normalized - 0.5f) * 2)
            }
        }
    }
    
    /**
     * 根据在线状态获取颜色
     */
    fun getStatusColor(isOnline: Boolean): Color {
        return if (isOnline) {
            Color(0xFF00B578) // 绿色
        } else {
            Color(0xFFF53F3F) // 红色
        }
    }
    
    /**
     * 根据信号强度获取颜色
     */
    fun getSignalColor(strength: Int): Color {
        return when {
            strength >= -50 -> Color(0xFF00B578) // 强 - 绿色
            strength >= -70 -> Color(0xFFFF7D00) // 中 - 橙色
            else -> Color(0xFFF53F3F) // 弱 - 红色
        }
    }
}