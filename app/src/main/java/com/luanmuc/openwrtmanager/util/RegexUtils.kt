package com.luanmuc.openwrtmanager.util

import java.util.regex.Pattern

/**
 * 正则表达式工具类
 * 提供常用的正则表达式验证功能
 */
object RegexUtils {
    
    // 常用正则表达式
    private const val REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    private const val REGEX_PHONE_CN = "^1[3-9]\\d{9}$"
    private const val REGEX_PHONE_GENERAL = "^\\+?[0-9\\s-]{7,15}$"
    private const val REGEX_IPV4 = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    private const val REGEX_IPV6 = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    private const val REGEX_MAC = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    private const val REGEX_URL = "^https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_+.~#?&/=]*)$"
    private const val REGEX_PORT = "^(?:[0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$"
    private const val REGEX_USERNAME = "^[a-zA-Z0-9_-]{3,20}$"
    private const val REGEX_PASSWORD_STRONG = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    private const val REGEX_PASSWORD_MEDIUM = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$"
    private const val REGEX_ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$"
    private const val REGEX_POSTAL_CODE = "^[1-9]\\d{5}$"
    private const val REGEX_NUMBER = "^-?\\d+(\\.\\d+)?$"
    const val REGEX_POSITIVE_INTEGER = "^[1-9]\\d*$"
    private const val REGEX_NEGATIVE_INTEGER = "^-[1-9]\\d*$"
    private const val REGEX_DECIMAL = "^-?\\d+\\.\\d+$"
    private const val REGEX_HEX_COLOR = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
    private const val REGEX_DATE_YYYY_MM_DD = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
    private const val REGEX_TIME_HH_MM_SS = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$"
    private const val REGEX_DOMAIN = "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$"
    private const val REGEX_HOSTNAME = "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$"
    
    // 预编译的Pattern
    private val emailPattern = Pattern.compile(REGEX_EMAIL)
    private val phoneCnPattern = Pattern.compile(REGEX_PHONE_CN)
    private val phoneGeneralPattern = Pattern.compile(REGEX_PHONE_GENERAL)
    private val ipv4Pattern = Pattern.compile(REGEX_IPV4)
    private val ipv6Pattern = Pattern.compile(REGEX_IPV6)
    private val macPattern = Pattern.compile(REGEX_MAC)
    private val urlPattern = Pattern.compile(REGEX_URL)
    private val portPattern = Pattern.compile(REGEX_PORT)
    private val usernamePattern = Pattern.compile(REGEX_USERNAME)
    private val passwordStrongPattern = Pattern.compile(REGEX_PASSWORD_STRONG)
    private val passwordMediumPattern = Pattern.compile(REGEX_PASSWORD_MEDIUM)
    private val idCardPattern = Pattern.compile(REGEX_ID_CARD)
    private val postalCodePattern = Pattern.compile(REGEX_POSTAL_CODE)
    private val numberPattern = Pattern.compile(REGEX_NUMBER)
    private val positiveIntegerPattern = Pattern.compile(REGEX_POSITIVE_INTEGER)
    private val negativeIntegerPattern = Pattern.compile(REGEX_NEGATIVE_INTEGER)
    private val decimalPattern = Pattern.compile(REGEX_DECIMAL)
    private val hexColorPattern = Pattern.compile(REGEX_HEX_COLOR)
    private val datePattern = Pattern.compile(REGEX_DATE_YYYY_MM_DD)
    private val timePattern = Pattern.compile(REGEX_TIME_HH_MM_SS)
    private val domainPattern = Pattern.compile(REGEX_DOMAIN)
    private val hostnamePattern = Pattern.compile(REGEX_HOSTNAME)
    
    /**
     * 验证邮箱
     */
    fun isEmail(input: String): Boolean {
        return emailPattern.matcher(input).matches()
    }
    
    /**
     * 验证中国手机号
     */
    fun isPhoneCn(input: String): Boolean {
        return phoneCnPattern.matcher(input).matches()
    }
    
    /**
     * 验证通用手机号
     */
    fun isPhoneGeneral(input: String): Boolean {
        return phoneGeneralPattern.matcher(input).matches()
    }
    
    /**
     * 验证IPv4地址
     */
    fun isIpv4(input: String): Boolean {
        return ipv4Pattern.matcher(input).matches()
    }
    
    /**
     * 验证IPv6地址
     */
    fun isIpv6(input: String): Boolean {
        return ipv6Pattern.matcher(input).matches()
    }
    
    /**
     * 验证IP地址（IPv4或IPv6）
     */
    fun isIpAddress(input: String): Boolean {
        return isIpv4(input) || isIpv6(input)
    }
    
    /**
     * 验证MAC地址
     */
    fun isMacAddress(input: String): Boolean {
        return macPattern.matcher(input).matches()
    }
    
    /**
     * 验证URL
     */
    fun isUrl(input: String): Boolean {
        return urlPattern.matcher(input).matches()
    }
    
    /**
     * 验证端口号
     */
    fun isPort(input: String): Boolean {
        return portPattern.matcher(input).matches()
    }
    
    /**
     * 验证端口号（整数）
     */
    fun isPort(port: Int): Boolean {
        return port in 0..65535
    }
    
    /**
     * 验证用户名
     * 规则：3-20位字母、数字、下划线、短横线
     */
    fun isUsername(input: String): Boolean {
        return usernamePattern.matcher(input).matches()
    }
    
    /**
     * 验证强密码
     * 规则：至少8位，包含大小写字母、数字和特殊字符
     */
    fun isStrongPassword(input: String): Boolean {
        return passwordStrongPattern.matcher(input).matches()
    }
    
    /**
     * 验证中等强度密码
     * 规则：至少6位，包含大小写字母和数字
     */
    fun isMediumPassword(input: String): Boolean {
        return passwordMediumPattern.matcher(input).matches()
    }
    
    /**
     * 验证身份证号
     */
    fun isIdCard(input: String): Boolean {
        return idCardPattern.matcher(input).matches()
    }
    
    /**
     * 验证邮政编码
     */
    fun isPostalCode(input: String): Boolean {
        return postalCodePattern.matcher(input).matches()
    }
    
    /**
     * 验证数字（整数或小数）
     */
    fun isNumber(input: String): Boolean {
        return numberPattern.matcher(input).matches()
    }
    
    /**
     * 验证正整数
     */
    fun isPositiveInteger(input: String): Boolean {
        return positiveIntegerPattern.matcher(input).matches()
    }
    
    /**
     * 验证负整数
     */
    fun isNegativeInteger(input: String): Boolean {
        return negativeIntegerPattern.matcher(input).matches()
    }
    
    /**
     * 验证整数
     */
    fun isInteger(input: String): Boolean {
        return isPositiveInteger(input) || isNegativeInteger(input) || input == "0"
    }
    
    /**
     * 验证小数
     */
    fun isDecimal(input: String): Boolean {
        return decimalPattern.matcher(input).matches()
    }
    
    /**
     * 验证十六进制颜色
     */
    fun isHexColor(input: String): Boolean {
        return hexColorPattern.matcher(input).matches()
    }
    
    /**
     * 验证日期（YYYY-MM-DD格式）
     */
    fun isDate(input: String): Boolean {
        return datePattern.matcher(input).matches()
    }
    
    /**
     * 验证时间（HH:MM:SS格式）
     */
    fun isTime(input: String): Boolean {
        return timePattern.matcher(input).matches()
    }
    
    /**
     * 验证域名
     */
    fun isDomain(input: String): Boolean {
        return domainPattern.matcher(input).matches()
    }
    
    /**
     * 验证主机名
     */
    fun isHostname(input: String): Boolean {
        return hostnamePattern.matcher(input).matches()
    }
    
    /**
     * 验证主机名或IP地址
     */
    fun isHostnameOrIp(input: String): Boolean {
        return isHostname(input) || isIpAddress(input)
    }
    
    /**
     * 检查是否包含中文
     */
    fun containsChinese(input: String): Boolean {
        return input.any { it.code in 0x4E00..0x9FA5 }
    }
    
    /**
     * 检查是否全是中文
     */
    fun isAllChinese(input: String): Boolean {
        return input.all { it.code in 0x4E00..0x9FA5 }
    }
    
    /**
     * 检查是否包含英文
     */
    fun containsEnglish(input: String): Boolean {
        return input.any { it.isLetter() }
    }
    
    /**
     * 检查是否全是英文
     */
    fun isAllEnglish(input: String): Boolean {
        return input.all { it.isLetter() }
    }
    
    /**
     * 检查是否包含数字
     */
    fun containsDigit(input: String): Boolean {
        return input.any { it.isDigit() }
    }
    
    /**
     * 检查是否全是数字
     */
    fun isAllDigit(input: String): Boolean {
        return input.all { it.isDigit() }
    }
    
    /**
     * 检查是否包含特殊字符
     */
    fun containsSpecialChar(input: String): Boolean {
        return input.any { !it.isLetterOrDigit() }
    }
    
    /**
     * 检查是否是空白字符串
     */
    fun isBlank(input: String): Boolean {
        return input.isBlank()
    }
    
    /**
     * 检查字符串长度是否在范围内
     */
    fun isLengthInRange(input: String, min: Int, max: Int): Boolean {
        return input.length in min..max
    }
    
    /**
     * 检查字符串最小长度
     */
    fun hasMinLength(input: String, min: Int): Boolean {
        return input.length >= min
    }
    
    /**
     * 检查字符串最大长度
     */
    fun hasMaxLength(input: String, max: Int): Boolean {
        return input.length <= max
    }
    
    /**
     * 提取字符串中的数字
     */
    fun extractNumbers(input: String): String {
        return input.filter { it.isDigit() }
    }
    
    /**
     * 提取字符串中的字母
     */
    fun extractLetters(input: String): String {
        return input.filter { it.isLetter() }
    }
    
    /**
     * 提取字符串中的中文
     */
    fun extractChinese(input: String): String {
        return input.filter { it.code in 0x4E00..0x9FA5 }
    }
    
    /**
     * 统计中文字符数
     */
    fun countChinese(input: String): Int {
        return input.count { it.code in 0x4E00..0x9FA5 }
    }
    
    /**
     * 统计英文字符数
     */
    fun countEnglish(input: String): Int {
        return input.count { it.isLetter() }
    }
    
    /**
     * 统计数字字符数
     */
    fun countDigits(input: String): Int {
        return input.count { it.isDigit() }
    }
    
    /**
     * 统计特殊字符数
     */
    fun countSpecialChars(input: String): Int {
        return input.count { !it.isLetterOrDigit() }
    }
    
    /**
     * 验证自定义正则表达式
     */
    fun matches(input: String, regex: String): Boolean {
        return try {
            Pattern.compile(regex).matcher(input).matches()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 查找所有匹配项
     */
    fun findAll(input: String, regex: String): List<String> {
        return try {
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(input)
            val results = mutableListOf<String>()
            while (matcher.find()) {
                results.add(matcher.group())
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 替换所有匹配项
     */
    fun replaceAll(input: String, regex: String, replacement: String): String {
        return try {
            Pattern.compile(regex).matcher(input).replaceAll(replacement)
        } catch (e: Exception) {
            input
        }
    }
    
    /**
     * 检查是否以指定前缀开头
     */
    fun startsWith(input: String, prefix: String, ignoreCase: Boolean = false): Boolean {
        return if (ignoreCase) {
            input.startsWith(prefix, true)
        } else {
            input.startsWith(prefix)
        }
    }
    
    /**
     * 检查是否以指定后缀结尾
     */
    fun endsWith(input: String, suffix: String, ignoreCase: Boolean = false): Boolean {
        return if (ignoreCase) {
            input.endsWith(suffix, true)
        } else {
            input.endsWith(suffix)
        }
    }
    
    /**
     * 检查是否包含指定子串
     */
    fun contains(input: String, substring: String, ignoreCase: Boolean = false): Boolean {
        return if (ignoreCase) {
            input.contains(substring, true)
        } else {
            input.contains(substring)
        }
    }
    
    /**
     * 隐藏手机号中间四位
     */
    fun maskPhone(phone: String): String {
        return if (isPhoneCn(phone)) {
            phone.substring(0, 3) + "****" + phone.substring(7)
        } else {
            phone
        }
    }
    
    /**
     * 隐藏邮箱中间部分
     */
    fun maskEmail(email: String): String {
        if (!isEmail(email)) return email
        
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email
        
        val username = email.substring(0, atIndex)
        val domain = email.substring(atIndex)
        
        return if (username.length <= 2) {
            username + "***" + domain
        } else {
            username.substring(0, 2) + "***" + domain
        }
    }
    
    /**
     * 隐藏身份证号中间部分
     */
    fun maskIdCard(idCard: String): String {
        if (!isIdCard(idCard)) return idCard
        return idCard.substring(0, 6) + "********" + idCard.substring(14)
    }
}