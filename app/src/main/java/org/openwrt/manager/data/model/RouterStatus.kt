package org.openwrt.manager.data.model

/**
 * 路由器系统状态信息
 */
data class RouterStatus(
    val hostname: String,
    val model: String,
    val firmware: String,
    val kernel: String,
    val uptime: Long,
    val loadAverage: List<Float>,
    val memoryTotal: Long,
    val memoryFree: Long,
    val memoryCached: Long,
    val memoryBuffered: Long,
    val cpuUsage: Float,
    val temperature: Float? = null,
    val wanIp: String? = null,
    val lanIp: String? = null,
    val clientCount: Int = 0
)
