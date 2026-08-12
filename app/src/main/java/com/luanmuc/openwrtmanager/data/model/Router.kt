package com.luanmuc.openwrtmanager.data.model

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
    val lastConnected: Long = 0L,
    // 远程管理配置
    val remoteEnabled: Boolean = false,
    val remoteAddress: String = "",
    val remotePort: Int = 443,
    val remoteProtocol: String = "https",
    val autoSwitchRemote: Boolean = true
) {
    /**
     * 获取当前应使用的地址
     * @param isRemoteMode 是否远程模式
     */
    fun getEffectiveAddress(isRemoteMode: Boolean): String {
        return if (isRemoteMode && remoteEnabled && remoteAddress.isNotBlank()) {
            "${remoteProtocol}://${remoteAddress}:${remotePort}"
        } else {
            address
        }
    }
}
