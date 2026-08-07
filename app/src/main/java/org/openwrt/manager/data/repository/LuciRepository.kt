package org.openwrt.manager.data.repository

import org.openwrt.manager.data.api.LuciApiService
import org.openwrt.manager.data.api.RetrofitClient
import org.openwrt.manager.data.model.LuciRpcRequest
import org.openwrt.manager.data.model.RouterStatus

/**
 * LuCI API 仓库
 * 封装所有与 OpenWrt LuCI ubus RPC 的交互
 * 支持 ImmortalWrt / OpenWrt 21.02+ 新版 LuCI
 */
class LuciRepository {
    private var authToken: String = ""
    private var currentAddress: String = ""

    /**
     * 登录认证
     * 使用 ubus session.login 方法
     * @param address 路由器地址
     * @param username 用户名
     * @param password 密码（支持空密码）
     * @return 认证token (ubus_rpc_session)
     */
    suspend fun login(address: String, username: String, password: String): String {
        currentAddress = normalizeAddress(address)
        val api = RetrofitClient.getApi(currentAddress)

        val request = LuciRpcRequest.loginRequest(username, password)
        val response = api.call(LuciApiService.UBUS_PATH, request)

        if (response.error != null) {
            throw Exception("认证失败: ${response.error.message}")
        }

        // ubus RPC 响应格式: result = [statusCode, data]
        // statusCode 0 表示成功
        val result = response.result
        if (result == null || result.size < 2) {
            throw Exception("认证失败：无效的响应格式")
        }

        val statusCode = (result[0] as? Number)?.toInt() ?: -1
        if (statusCode != 0) {
            throw Exception("认证失败：用户名或密码错误 (错误码: $statusCode)")
        }

        val data = result[1] as? Map<*, *>
        authToken = data?.get("ubus_rpc_session")?.toString() ?: ""

        if (authToken.isEmpty()) {
            throw Exception("认证失败：未获取到会话令牌")
        }

        return authToken
    }

    /**
     * 调用 ubus 方法
     */
    private suspend fun callUbus(
        obj: String,
        method: String,
        params: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        val api = RetrofitClient.getApi(currentAddress)

        val request = LuciRpcRequest.callRequest(authToken, obj, method, params)
        val response = api.call(LuciApiService.UBUS_PATH, request)

        if (response.error != null) {
            throw Exception("调用失败: ${response.error.message}")
        }

        val result = response.result
        if (result == null || result.size < 2) {
            throw Exception("无效的响应格式")
        }

        val statusCode = (result[0] as? Number)?.toInt() ?: -1
        if (statusCode != 0) {
            throw Exception("调用失败 (错误码: $statusCode)")
        }

        @Suppress("UNCHECKED_CAST")
        return result[1] as? Map<String, Any> ?: emptyMap()
    }

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): Map<String, Any> {
        return try {
            callUbus("system", "info")
        } catch (e: Exception) {
            // 尝试备用方式获取信息
            emptyMap()
        }
    }

    /**
     * 获取路由器状态
     */
    suspend fun getRouterStatus(): RouterStatus {
        val sysInfo = getSystemInfo()
        val boardInfo = try {
            callUbus("system", "board")
        } catch (e: Exception) {
            emptyMap()
        }

        val hostname = sysInfo["hostname"]?.toString()
            ?: boardInfo["hostname"]?.toString()
            ?: "OpenWrt"

        val model = boardInfo["model"]?.toString()
            ?: sysInfo["model"]?.toString()
            ?: "Unknown"

        val release = boardInfo["release"]?.toString()
            ?: sysInfo["release"]?.toString()
            ?: "Unknown"

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

        val cpuUsage = calculateCpuUsage(sysInfo)

        return RouterStatus(
            hostname = hostname,
            model = model,
            firmware = release,
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
     * 重启路由器
     */
    suspend fun reboot(): Boolean {
        return try {
            callUbus("system", "reboot")
            true
        } catch (e: Exception) {
            // 重启时连接会断开，视为成功
            true
        }
    }

    private fun calculateCpuUsage(sysInfo: Map<String, Any>): Float {
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
