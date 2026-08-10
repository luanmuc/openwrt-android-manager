package com.luanmuc.openwrtmanager.util

import android.util.Patterns
import java.util.regex.Pattern

/**
 * 验证工具类
 * 提供各种输入验证功能
 */
object ValidationUtils {
    
    // 用户名正则：3-20个字符，字母、数字、下划线
    private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$")
    
    // 密码正则：至少6个字符，包含字母和数字
    private val PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")
    
    // 端口号范围
    private const val MIN_PORT = 1
    private const val MAX_PORT = 65535
    
    /**
     * 验证邮箱地址
     */
    fun isValidEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
    
    /**
     * 验证手机号（中国）
     */
    fun isValidPhone(phone: String): Boolean {
        return if (phone.isEmpty()) {
            false
        } else {
            Patterns.PHONE.matcher(phone).matches()
        }
    }
    
    /**
     * 验证URL
     */
    fun isValidUrl(url: String): Boolean {
        return if (url.isEmpty()) {
            false
        } else {
            Patterns.WEB_URL.matcher(url).matches()
        }
    }
    
    /**
     * 验证IP地址（IPv4）
     */
    fun isValidIpv4(ip: String): Boolean {
        if (ip.isEmpty()) return false
        
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            part.toIntOrNull()?.let { num ->
                num in 0..255 && (part == "0" || !part.startsWith("0") || part.length == 1)
            } ?: false
        }
    }
    
    /**
     * 验证IP地址（IPv6）
     */
    fun isValidIpv6(ip: String): Boolean {
        if (ip.isEmpty()) return false
        
        // 简单的IPv6验证
        val parts = ip.split(":")
        if (parts.size !in 2..8) return false
        
        return parts.all { part ->
            if (part.isEmpty()) {
                true // 允许空段（::）
            } else {
                part.length <= 4 && part.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
            }
        }
    }
    
    /**
     * 验证MAC地址
     */
    fun isValidMac(mac: String): Boolean {
        if (mac.isEmpty()) return false
        
        val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
        return mac.matches(Regex(macRegex))
    }
    
    /**
     * 验证端口号
     */
    fun isValidPort(port: Int): Boolean {
        return port in MIN_PORT..MAX_PORT
    }
    
    /**
     * 验证端口号（字符串）
     */
    fun isValidPort(port: String): Boolean {
        return port.toIntOrNull()?.let { isValidPort(it) } ?: false
    }
    
    /**
     * 验证用户名
     */
    fun isValidUsername(username: String): Boolean {
        return if (username.isEmpty()) {
            false
        } else {
            USERNAME_PATTERN.matcher(username).matches()
        }
    }
    
    /**
     * 验证密码强度
     */
    fun isValidPassword(password: String): Boolean {
        return if (password.isEmpty()) {
            false
        } else {
            password.length >= 6
        }
    }
    
    /**
     * 验证强密码
     */
    fun isStrongPassword(password: String): Boolean {
        return if (password.isEmpty()) {
            false
        } else {
            PASSWORD_PATTERN.matcher(password).matches()
        }
    }
    
    /**
     * 获取密码强度等级
     * 0: 弱, 1: 中, 2: 强, 3: 非常强
     */
    fun getPasswordStrength(password: String): Int {
        if (password.isEmpty()) return 0
        
        var strength = 0
        
        // 长度
        if (password.length >= 6) strength++
        if (password.length >= 10) strength++
        
        // 包含数字
        if (password.any { it.isDigit() }) strength++
        
        // 包含小写字母
        if (password.any { it.isLowerCase() }) strength++
        
        // 包含大写字母
        if (password.any { it.isUpperCase() }) strength++
        
        // 包含特殊字符
        if (password.any { !it.isLetterOrDigit() }) strength++
        
        return when {
            strength <= 2 -> 0 // 弱
            strength <= 3 -> 1 // 中
            strength <= 4 -> 2 // 强
            else -> 3 // 非常强
        }
    }
    
    /**
     * 验证主机名
     */
    fun isValidHostname(hostname: String): Boolean {
        if (hostname.isEmpty()) return false
        if (hostname.length > 253) return false
        
        val labels = hostname.split(".")
        return labels.all { label ->
            label.isNotEmpty() && 
            label.length <= 63 &&
            label.matches(Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$"))
        }
    }
    
    /**
     * 验证子网掩码
     */
    fun isValidSubnetMask(mask: String): Boolean {
        if (!isValidIpv4(mask)) return false
        
        val parts = mask.split(".").map { it.toInt() }
        var binary = ""
        
        for (part in parts) {
            binary += part.toString(2).padStart(8, '0')
        }
        
        // 子网掩码必须是连续的1后面跟着连续的0
        var foundZero = false
        for (bit in binary) {
            if (bit == '0') {
                foundZero = true
            } else if (foundZero) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * 验证非空
     */
    fun isNotEmpty(value: String?): Boolean {
        return !value.isNullOrEmpty() && value.isNotBlank()
    }
    
    /**
     * 验证长度范围
     */
    fun isLengthInRange(value: String, min: Int, max: Int): Boolean {
        return value.length in min..max
    }
    
    /**
     * 验证数字范围
     */
    fun isNumberInRange(value: Int, min: Int, max: Int): Boolean {
        return value in min..max
    }
    
    /**
     * 验证是否是数字
     */
    fun isNumeric(value: String): Boolean {
        return value.toIntOrNull() != null || value.toLongOrNull() != null
    }
    
    /**
     * 验证是否是浮点数
     */
    fun isFloat(value: String): Boolean {
        return value.toFloatOrNull() != null || value.toDoubleOrNull() != null
    }
}