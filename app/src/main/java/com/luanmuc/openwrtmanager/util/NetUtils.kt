package com.luanmuc.openwrtmanager.util

import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

/**
 * 网络工具类
 * 提供IP地址、MAC地址、网络状态等工具功能
 */
object NetUtils {
    
    /**
     * 验证IP地址格式
     */
    fun isValidIpAddress(ip: String): Boolean {
        if (ip.isEmpty()) return false
        
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return try {
            parts.all { part ->
                val num = part.toInt()
                num in 0..255
            }
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * 验证MAC地址格式
     */
    fun isValidMacAddress(mac: String): Boolean {
        if (mac.isEmpty()) return false
        
        val regex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return regex.matches(mac)
    }
    
    /**
     * 验证端口号
     */
    fun isValidPort(port: Int): Boolean {
        return port in 1..65535
    }
    
    /**
     * 验证端口号（字符串）
     */
    fun isValidPort(port: String): Boolean {
        return try {
            isValidPort(port.toInt())
        } catch (e: NumberFormatException) {
            false
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
            label.matches("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$".toRegex())
        }
    }
    
    /**
     * 验证URL
     */
    fun isValidUrl(url: String): Boolean {
        if (url.isEmpty()) return false
        
        return try {
            val uri = java.net.URI(url)
            uri.scheme != null && uri.host != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取本地IP地址
     */
    fun getLocalIpAddress(): String? {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (netInterface in interfaces) {
                val addresses = Collections.list(netInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val sAddr = address.hostAddress
                        if (sAddr != null && sAddr.indexOf(':') < 0) {
                            return sAddr
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 检查是否是私有IP地址
     */
    fun isPrivateIp(ip: String): Boolean {
        if (!isValidIpAddress(ip)) return false
        
        val parts = ip.split(".").map { it.toInt() }
        if (parts.size < 2) return false
        
        // 10.0.0.0/8
        if (parts[0] == 10) return true
        
        // 172.16.0.0/12
        if (parts[0] == 172 && parts[1] in 16..31) return true
        
        // 192.168.0.0/16
        if (parts[0] == 192 && parts[1] == 168) return true
        
        // 127.0.0.0/8 (loopback)
        if (parts[0] == 127) return true
        
        // 169.254.0.0/16 (link-local)
        if (parts[0] == 169 && parts[1] == 254) return true
        
        return false
    }
    
    /**
     * 格式化URL（添加协议前缀）
     */
    fun formatUrl(url: String): String {
        var formatted = url.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://$formatted"
        }
        return formatted
    }
    
    /**
     * 从URL中提取主机名
     */
    fun extractHost(url: String): String? {
        return try {
            val uri = java.net.URI(url)
            uri.host
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从URL中提取端口
     */
    fun extractPort(url: String): Int {
        return try {
            val uri = java.net.URI(url)
            if (uri.port != -1) uri.port else if (uri.scheme == "https") 443 else 80
        } catch (e: Exception) {
            80
        }
    }
    
    /**
     * Ping主机（简单实现）
     */
    fun ping(host: String, timeout: Int = 3000): Boolean {
        return try {
            val address = InetAddress.getByName(host)
            address.isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }
}