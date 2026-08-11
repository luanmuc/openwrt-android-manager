package com.luanmuc.openwrtmanager.util

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

/**
 * 数学工具类
 * 提供常用的数学计算功能
 */
object MathUtils {
    
    /**
     * 四舍五入到指定小数位
     */
    fun roundTo(value: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return round(value * factor) / factor
    }
    
    /**
     * 向上取整到指定小数位
     */
    fun ceilTo(value: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return ceil(value * factor) / factor
    }
    
    /**
     * 向下取整到指定小数位
     */
    fun floorTo(value: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return floor(value * factor) / factor
    }
    
    /**
     * 截断到指定小数位
     */
    fun truncateTo(value: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return (if (value >= 0) floor(value * factor) else ceil(value * factor)) / factor
    }
    
    /**
     * 检查是否接近零
     */
    fun isNearZero(value: Double, epsilon: Double = 1e-10): Boolean {
        return abs(value) < epsilon
    }
    
    /**
     * 检查两个数是否近似相等
     */
    fun isApproximatelyEqual(a: Double, b: Double, epsilon: Double = 1e-10): Boolean {
        return abs(a - b) < epsilon
    }
    
    /**
     * 计算平均值
     */
    fun average(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        return values.sum() / values.size
    }
    
    /**
     * 计算平均值（List）
     */
    fun average(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        return values.sum() / values.size
    }
    
    /**
     * 计算平均值（Int）
     */
    fun average(vararg values: Int): Double {
        if (values.isEmpty()) return 0.0
        return values.average()
    }
    
    /**
     * 计算中位数
     */
    fun median(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }
    
    /**
     * 计算中位数（List）
     */
    fun median(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }
    
    /**
     * 计算方差
     */
    fun variance(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        val mean = average(*values)
        return values.map { (it - mean).pow(2) }.average()
    }
    
    /**
     * 计算标准差
     */
    fun standardDeviation(vararg values: Double): Double {
        return sqrt(variance(*values))
    }
    
    /**
     * 计算标准差（List）
     */
    fun standardDeviation(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = average(values)
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }
    
    /**
     * 计算最大值
     */
    fun max(vararg values: Double): Double {
        return values.maxOrNull() ?: 0.0
    }
    
    /**
     * 计算最小值
     */
    fun min(vararg values: Double): Double {
        return values.minOrNull() ?: 0.0
    }
    
    /**
     * 计算范围（最大值-最小值）
     */
    fun range(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        return max(*values) - min(*values)
    }
    
    /**
     * 计算总和
     */
    fun sum(vararg values: Double): Double {
        return values.sum()
    }
    
    /**
     * 计算总和（Int）
     */
    fun sum(vararg values: Int): Int {
        return values.sum()
    }
    
    /**
     * 计算乘积
     */
    fun product(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        return values.fold(1.0) { acc, value -> acc * value }
    }
    
    /**
     * 计算阶乘
     */
    fun factorial(n: Int): Long {
        if (n < 0) return 0
        if (n == 0 || n == 1) return 1
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }
    
    /**
     * 计算最大公约数
     */
    fun gcd(a: Int, b: Int): Int {
        var x = abs(a)
        var y = abs(b)
        while (y != 0) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }
    
    /**
     * 计算最小公倍数
     */
    fun lcm(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return abs(a * b) / gcd(a, b)
    }
    
    /**
     * 检查是否为素数
     */
    fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0 || n % 3 == 0) return false
        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false
            i += 6
        }
        return true
    }
    
    /**
     * 检查是否为偶数
     */
    fun isEven(n: Int): Boolean {
        return n % 2 == 0
    }
    
    /**
     * 检查是否为奇数
     */
    fun isOdd(n: Int): Boolean {
        return n % 2 != 0
    }
    
    /**
     * 检查是否为完全平方数
     */
    fun isPerfectSquare(n: Int): Boolean {
        if (n < 0) return false
        val sqrt = sqrt(n.toDouble()).toInt()
        return sqrt * sqrt == n
    }
    
    /**
     * 计算百分比
     */
    fun percentage(part: Double, total: Double): Double {
        return if (total == 0.0) 0.0 else (part / total) * 100
    }
    
    /**
     * 计算百分比变化
     */
    fun percentageChange(oldValue: Double, newValue: Double): Double {
        return if (oldValue == 0.0) 0.0 else ((newValue - oldValue) / oldValue) * 100
    }
    
    /**
     * 计算折扣后价格
     */
    fun discount(price: Double, discountPercentage: Double): Double {
        return price * (1 - discountPercentage / 100)
    }
    
    /**
     * 计算加价后价格
     */
    fun markup(price: Double, markupPercentage: Double): Double {
        return price * (1 + markupPercentage / 100)
    }
    
    /**
     * 计算复利
     */
    fun compoundInterest(principal: Double, rate: Double, years: Int, compoundsPerYear: Int = 12): Double {
        return principal * (1 + rate / 100 / compoundsPerYear).pow(compoundsPerYear * years)
    }
    
    /**
     * 计算简单利息
     */
    fun simpleInterest(principal: Double, rate: Double, years: Int): Double {
        return principal * rate / 100 * years
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
    fun map(
        value: Double,
        fromMin: Double,
        fromMax: Double,
        toMin: Double,
        toMax: Double
    ): Double {
        val fraction = (value - fromMin) / (fromMax - fromMin)
        return lerp(toMin, toMax, fraction)
    }
    
    /**
     * 限制值在范围内
     */
    fun clamp(value: Double, min: Double, max: Double): Double {
        return min(max(value, min), max)
    }
    
    /**
     * 限制值在范围内（Int）
     */
    fun clamp(value: Int, min: Int, max: Int): Int {
        return min(max(value, min), max)
    }
    
    /**
     * 计算两点之间的距离
     */
    fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
    
    /**
     * 计算两点之间的曼哈顿距离
     */
    fun manhattanDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return abs(x2 - x1) + abs(y2 - y1)
    }
    
    /**
     * 计算角度（弧度）
     */
    fun angle(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return kotlin.math.atan2(y2 - y1, x2 - x1)
    }
    
    /**
     * 弧度转角度
     */
    fun toDegrees(radians: Double): Double {
        return Math.toDegrees(radians)
    }
    
    /**
     * 角度转弧度
     */
    fun toRadians(degrees: Double): Double {
        return Math.toRadians(degrees)
    }
    
    /**
     * 计算移动平均值
     */
    fun movingAverage(values: List<Double>, windowSize: Int): List<Double> {
        if (values.isEmpty() || windowSize <= 0) return emptyList()
        val result = mutableListOf<Double>()
        for (i in values.indices) {
            val start = max(0, i - windowSize + 1)
            val sublist = values.subList(start, i + 1)
            result.add(sublist.average())
        }
        return result
    }
    
    /**
     * 归一化数据（0-1范围）
     */
    fun normalize(values: List<Double>): List<Double> {
        if (values.isEmpty()) return emptyList()
        val min = values.minOrNull() ?: return values
        val max = values.maxOrNull() ?: return values
        val range = max - min
        if (range == 0.0) return values.map { 0.5 }
        return values.map { (it - min) / range }
    }
    
    /**
     * 标准化数据（均值0，标准差1）
     */
    fun standardize(values: List<Double>): List<Double> {
        if (values.isEmpty()) return emptyList()
        val mean = average(values)
        val std = standardDeviation(values)
        if (std == 0.0) return values.map { 0.0 }
        return values.map { (it - mean) / std }
    }
}