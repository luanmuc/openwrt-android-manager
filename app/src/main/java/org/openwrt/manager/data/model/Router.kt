package org.openwrt.manager.data.model

/**
 * 路由器设备信息
 */
data class Router(
    val id: String,
    val name: String,
    val address: String,
    val username: String,
    val encryptedPassword: String,
    val isConnected: Boolean = false,
    val lastConnected: Long = 0L
)
