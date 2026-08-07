package org.openwrt.manager.data.repository

import org.openwrt.manager.data.api.LuciApiService
import org.openwrt.manager.data.api.RetrofitClient
import org.openwrt.manager.data.model.LuciRpcRequest
import org.openwrt.manager.data.model.LuciRpcResponse
import org.openwrt.manager.data.model.RouterStatus

/**
 * LuCI API 仓库
 * 封装所有与 OpenWrt LuCI RPC 的交互
 */
class LuciRepository {

    private var authToken: String = ""
    private var currentAddress: String = ""

    /**
     * 登录认证
     * @param address 路由器地址
     * @param username 用户名
     * @param password 密码
     * @return 认证token
     */
    suspend fun login(address: String, username: String, password: String): String {
        currentAddress = normalizeAddress(address)
        val api = RetrofitClient.getApi(currentAddress)

        val request = LuciRpcRequest(
            id = 1,
            method = "login",
            params = listOf(username, password)
        )

        val response = api.call(LuciApiService.AUTH_PATH, request)
        if (response.error != null) {
            throw Exception("认证失败: ${response.error.message}")
        }

        authToken = response.result?.toString() ?: ""
        if (authToken.isEmpty()) {
            throw Exception("认证失败：无效的响应")
        }

        return authToken
    }

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): Map<String, Any> {
        val api = RetrofitClient.getApi(currentAddress)

        val request = LuciRpcRequest(
            id = 2,
            method = "sysinfo",
            params = listOf(authToken)
        )

        val response = api.call(LuciApiService.SYS_PATH, request)
        if (response.error != null) {
            throw Exception("获取系统信息失败: ${response.error.message}")
        }

        @Suppress("UNCHECKED_CAST")
        return response.result as? Map<String, Any> ?: emptyMap()
    }

    /**
     * 获取路由器状态
     */
    suspend fun getRouterStatus(): RouterStatus {
        val sysInfo = getSystemInfo()

        val hostname = sysInfo["hostname"]?.toString() ?: "Unknown"
        val model = sysInfo["model"]?.toString() ?: "Unknown"
        val firmware = sysInfo["release"]?.toString() ?: "Unknown"
        val kernel = sysInfo["kernel"]?.toString() ?: "Unknown"
        val uptime = (sysInfo["uptime"] as? Number)?.toLong() ?: 0L

        val loadList = sysInfo["load"] as? List<*>
        val loadAverage = loadList?.mapNotNull {
            (it as? Number)?.toFloat()
        } ?: listOf(0f, 0f, 0f)

        val memory = sysInfo["memory"] as? Map<*, *>
        val memoryTotal = (memory?.get("total") as? Number)?.toLong() ?: 0L
        val memoryFree = (memory?.get("free") as? Number)?.toLong() ?: 0L
        val memoryCached = (memory?.get("cached") as? Number)?.toLong() ?: 0L
        val memoryBuffered = (memory?.get("buffered") as? Number)?.toLong() ?: 0L

        // CPU 使用率计算（简化版）
        val cpuUsage = calculateCpuUsage(sysInfo)

        return RouterStatus(
            hostname = hostname,
            model = model,
            firmware = firmware,
            kernel = kernel,
            uptime = uptime,
            loadAverage = loadAverage,
            memoryTotal = memoryTotal,
            memoryFree = memoryFree,
            memoryCached = memoryCached,
            memoryBuffered = memoryBuffered,
            cpuUsage = cpuUsage
        )
    }

    /**
     * 执行系统命令（需要权限）
     */
    suspend fun exec(command: String): String {
        val api = RetrofitClient.getApi(currentAddress)

        val request = LuciRpcRequest(
            id = 3,
            method = "exec",
            params = listOf(authToken, command)
        )

        val response = api.call(LuciApiService.SYS_PATH, request)
        if (response.error != null) {
            throw Exception("执行命令失败: ${response.error.message}")
        }

        return response.result?.toString() ?: ""
    }

    /**
     * 重启路由器
     */
    suspend fun reboot(): Boolean {
        return try {
            exec("reboot")
            true
        } catch (e: Exception) {
            // 重启时连接会断开，视为成功
            true
        }
    }

    private fun calculateCpuUsage(sysInfo: Map<String, Any>): Float {
        // 简化计算，实际需要两次采样对比
        val cpu = sysInfo["cpu"] as? Map<*, *>
        return (cpu?.get("usage") as? Number)?.toFloat() ?: 0f
    }

    private fun normalizeAddress(address: String): String {
        var addr = address.trim()
        if (!addr.startsWith("http://") && !addr.startsWith("https://")) {
            addr = "http://$addr"
        }
        if (!addr.endsWith("/")) {
            addr = "$addr/"
        }
        return addr
    }

    fun getCurrentAddress(): String = currentAddress

    fun isLoggedIn(): Boolean = authToken.isNotEmpty()

    fun logout() {
        authToken = ""
        currentAddress = ""
        RetrofitClient.reset()
    }
}
