package com.luanmuc.openwrtmanager.util

/**
 * 单位转换工具类
 * 提供常用的单位转换功能
 */
object UnitConverter {
    
    // 存储单位
    private const val KB = 1024.0
    private const val MB = KB * 1024
    private const val GB = MB * 1024
    private const val TB = GB * 1024
    
    // 网速单位
    private const val KBPS = 1000.0
    private const val MBPS = KBPS * 1000
    private const val GBPS = MBPS * 1000
    
    // 温度单位
    private const val FAHRENHEIT_OFFSET = 32.0
    private const val FAHRENHEIT_FACTOR = 9.0 / 5.0
    
    // 长度单位
    private const val CM_PER_INCH = 2.54
    private const val M_PER_FOOT = 0.3048
    
    // 重量单位
    private const val KG_PER_POUND = 0.45359237
    
    /**
     * 字节转可读字符串
     */
    fun bytesToReadable(bytes: Long): String {
        return when {
            bytes >= TB -> String.format("%.2f TB", bytes / TB)
            bytes >= GB -> String.format("%.2f GB", bytes / GB)
            bytes >= MB -> String.format("%.2f MB", bytes / MB)
            bytes >= KB -> String.format("%.2f KB", bytes / KB)
            else -> "$bytes B"
        }
    }
    
    /**
     * 字节转可读字符串（简短版）
     */
    fun bytesToReadableShort(bytes: Long): String {
        return when {
            bytes >= TB -> String.format("%.1fT", bytes / TB)
            bytes >= GB -> String.format("%.1fG", bytes / GB)
            bytes >= MB -> String.format("%.1fM", bytes / MB)
            bytes >= KB -> String.format("%.1fK", bytes / KB)
            else -> "${bytes}B"
        }
    }
    
    /**
     * KB转可读字符串
     */
    fun kbToReadable(kb: Double): String {
        return bytesToReadable((kb * KB).toLong())
    }
    
    /**
     * MB转可读字符串
     */
    fun mbToReadable(mb: Double): String {
        return bytesToReadable((mb * MB).toLong())
    }
    
    /**
     * GB转可读字符串
     */
    fun gbToReadable(gb: Double): String {
        return bytesToReadable((gb * GB).toLong())
    }
    
    /**
     * 网速转可读字符串（bps）
     */
    fun bpsToReadable(bps: Double): String {
        return when {
            bps >= GBPS -> String.format("%.2f Gbps", bps / GBPS)
            bps >= MBPS -> String.format("%.2f Mbps", bps / MBPS)
            bps >= KBPS -> String.format("%.2f Kbps", bps / KBPS)
            else -> String.format("%.0f bps", bps)
        }
    }
    
    /**
     * 网速转可读字符串（简短版）
     */
    fun bpsToReadableShort(bps: Double): String {
        return when {
            bps >= GBPS -> String.format("%.1fG", bps / GBPS)
            bps >= MBPS -> String.format("%.1fM", bps / MBPS)
            bps >= KBPS -> String.format("%.1fK", bps / KBPS)
            else -> String.format("%.0f", bps)
        }
    }
    
    /**
     * 字节每秒转可读字符串
     */
    fun bytesPerSecondToReadable(bytesPerSecond: Double): String {
        return bpsToReadable(bytesPerSecond * 8)
    }
    
    /**
     * 摄氏度转华氏度
     */
    fun celsiusToFahrenheit(celsius: Double): Double {
        return celsius * FAHRENHEIT_FACTOR + FAHRENHEIT_OFFSET
    }
    
    /**
     * 华氏度转摄氏度
     */
    fun fahrenheitToCelsius(fahrenheit: Double): Double {
        return (fahrenheit - FAHRENHEIT_OFFSET) / FAHRENHEIT_FACTOR
    }
    
    /**
     * 摄氏度转开尔文
     */
    fun celsiusToKelvin(celsius: Double): Double {
        return celsius + 273.15
    }
    
    /**
     * 开尔文转摄氏度
     */
    fun kelvinToCelsius(kelvin: Double): Double {
        return kelvin - 273.15
    }
    
    /**
     * 格式化温度
     */
    fun formatTemperature(celsius: Double, unit: TemperatureUnit = TemperatureUnit.CELSIUS): String {
        return when (unit) {
            TemperatureUnit.CELSIUS -> String.format("%.1f°C", celsius)
            TemperatureUnit.FAHRENHEIT -> String.format("%.1f°F", celsiusToFahrenheit(celsius))
            TemperatureUnit.KELVIN -> String.format("%.1fK", celsiusToKelvin(celsius))
        }
    }
    
    /**
     * 英寸转厘米
     */
    fun inchesToCm(inches: Double): Double {
        return inches * CM_PER_INCH
    }
    
    /**
     * 厘米转英寸
     */
    fun cmToInches(cm: Double): Double {
        return cm / CM_PER_INCH
    }
    
    /**
     * 英尺转米
     */
    fun feetToMeters(feet: Double): Double {
        return feet * M_PER_FOOT
    }
    
    /**
     * 米转英尺
     */
    fun metersToFeet(meters: Double): Double {
        return meters / M_PER_FOOT
    }
    
    /**
     * 磅转千克
     */
    fun poundsToKg(pounds: Double): Double {
        return pounds * KG_PER_POUND
    }
    
    /**
     * 千克转磅
     */
    fun kgToPounds(kg: Double): Double {
        return kg / KG_PER_POUND
    }
    
    /**
     * 百分比格式化
     */
    fun formatPercentage(value: Double, decimalPlaces: Int = 1): String {
        return String.format("%.${decimalPlaces}f%%", value)
    }
    
    /**
     * 百分比格式化（整数）
     */
    fun formatPercentageInt(value: Double): String {
        return String.format("%.0f%%", value)
    }
    
    /**
     * 小数格式化
     */
    fun formatDecimal(value: Double, decimalPlaces: Int = 2): String {
        return String.format("%.${decimalPlaces}f", value)
    }
    
    /**
     * 数字格式化（带千分位）
     */
    fun formatNumberWithCommas(number: Long): String {
        return String.format("%,d", number)
    }
    
    /**
     * 数字格式化（带千分位，小数）
     */
    fun formatNumberWithCommas(number: Double, decimalPlaces: Int = 2): String {
        return String.format("%,.${decimalPlaces}f", number)
    }
    
    /**
     * 频率格式化
     */
    fun formatFrequency(hz: Double): String {
        return when {
            hz >= 1_000_000_000 -> String.format("%.2f GHz", hz / 1_000_000_000)
            hz >= 1_000_000 -> String.format("%.2f MHz", hz / 1_000_000)
            hz >= 1_000 -> String.format("%.2f KHz", hz / 1_000)
            else -> String.format("%.0f Hz", hz)
        }
    }
    
    /**
     * 功率格式化
     */
    fun formatPower(watts: Double): String {
        return when {
            watts >= 1_000_000 -> String.format("%.2f MW", watts / 1_000_000)
            watts >= 1_000 -> String.format("%.2f kW", watts / 1_000)
            else -> String.format("%.2f W", watts)
        }
    }
    
    /**
     * 电压格式化
     */
    fun formatVoltage(volts: Double): String {
        return when {
            volts >= 1_000_000 -> String.format("%.2f MV", volts / 1_000_000)
            volts >= 1_000 -> String.format("%.2f kV", volts / 1_000)
            else -> String.format("%.2f V", volts)
        }
    }
    
    /**
     * 电流格式化
     */
    fun formatCurrent(amps: Double): String {
        return when {
            amps >= 1_000_000 -> String.format("%.2f MA", amps / 1_000_000)
            amps >= 1_000 -> String.format("%.2f kA", amps / 1_000)
            amps >= 1 -> String.format("%.2f A", amps)
            amps >= 0.001 -> String.format("%.2f mA", amps * 1_000)
            else -> String.format("%.2f µA", amps * 1_000_000)
        }
    }
    
    /**
     * 温度单位枚举
     */
    enum class TemperatureUnit {
        CELSIUS,
        FAHRENHEIT,
        KELVIN
    }
    
    /**
     * 存储单位枚举
     */
    enum class StorageUnit {
        BYTES,
        KB,
        MB,
        GB,
        TB
    }
    
    /**
     * 转换存储单位
     */
    fun convertStorage(value: Double, from: StorageUnit, to: StorageUnit): Double {
        val bytes = when (from) {
            StorageUnit.BYTES -> value
            StorageUnit.KB -> value * KB
            StorageUnit.MB -> value * MB
            StorageUnit.GB -> value * GB
            StorageUnit.TB -> value * TB
        }
        
        return when (to) {
            StorageUnit.BYTES -> bytes
            StorageUnit.KB -> bytes / KB
            StorageUnit.MB -> bytes / MB
            StorageUnit.GB -> bytes / GB
            StorageUnit.TB -> bytes / TB
        }
    }
    
    /**
     * 计算百分比
     */
    fun calculatePercentage(part: Double, total: Double): Double {
        return if (total == 0.0) 0.0 else (part / total) * 100
    }
    
    /**
     * 计算百分比（Long）
     */
    fun calculatePercentage(part: Long, total: Long): Double {
        return calculatePercentage(part.toDouble(), total.toDouble())
    }
    
    /**
     * 限制值在范围内
     */
    fun clamp(value: Double, min: Double, max: Double): Double {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
    
    /**
     * 限制值在范围内（Int）
     */
    fun clamp(value: Int, min: Int, max: Int): Int {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
    
    /**
     * 线性插值
     */
    fun lerp(start: Double, end: Double, fraction: Double): Double {
        return start + (end - start) * fraction
    }
    
    /**
     * 映射值从一个范围到另一个范围
     */
    fun mapValue(
        value: Double,
        fromMin: Double,
        fromMax: Double,
        toMin: Double,
        toMax: Double
    ): Double {
        val fraction = (value - fromMin) / (fromMax - fromMin)
        return lerp(toMin, toMax, fraction)
    }
}