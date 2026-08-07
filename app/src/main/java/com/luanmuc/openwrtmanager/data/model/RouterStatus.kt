package com.luanmuc.openwrtmanager.data.model

/**
 * 路由器系统状态信息
 */
data class RouterStatus(
    val hostname: String = "",
    val model: String = "",
    val firmware: String = "",
    val kernel: String = "",
    val uptime: Long = 0,
    val loadAverage: List<Float> = emptyList(),
    val memoryTotal: Long = 0,
    val memoryFree: Long = 0,
    val memoryCached: Long = 0,
    val memoryBuffered: Long = 0,
    val memoryUsed: Long = 0,
    val storageTotal: Long = 0,
    val storageFree: Long = 0,
    val storageUsed: Long = 0,
    val cpuUsage: Float = 0f,
    val temperature: Float? = null,
    val wanIp: String? = null,
    val lanIp: String? = null,
    val wanConnected: Boolean = false,
    val wanUptime: Long = 0,
    val onlineDevices: Int = 0,
    val clientCount: Int = 0
)
