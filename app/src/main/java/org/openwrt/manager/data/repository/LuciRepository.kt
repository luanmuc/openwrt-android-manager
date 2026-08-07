package org.openwrt.manager.data.repository

import org.openwrt.manager.data.api.LuciApiService
import org.openwrt.manager.data.api.RetrofitClient
import org.openwrt.manager.data.model.LuciRpcRequest
import org.openwrt.manager.data.model.RouterStatus
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * LuCI API 仓库
 * 封装所有与 OpenWrt LuCI ubus RPC 的交互
 * 支持 ImmortalWrt / OpenWrt 21.02+ 新版 LuCI
 */
class LuciRepository {
    private var authToken: String = ""
    private var currentAddress: String = ""
    private var currentUsername: String = ""
    private var currentPassword: String = ""

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
        currentUsername = username
        currentPassword = password

        val api = RetrofitClient.getApi(currentAddress)

        try {
            val request = LuciRpcRequest.loginRequest(username, password)
            val response = api.call(LuciApiService.UBUS_PATH, request)

            if (response.error != null) {
                throw LuciException(
                    code = response.error.code,
                    message = response.error.message
                )
            }

            // ubus RPC 响应格式: result = [statusCode, data]
            // statusCode 0 表示成功
            val result = response.result
            if (result == null || result.size < 2) {
                throw LuciException(message = "无效的响应格式")
            }

            val statusCode = (result[0] as? Number)?.toInt() ?: -1
            if (statusCode != 0) {
                when (statusCode) {
                    -32000, -32002 -> throw LuciException(
                        code = statusCode,
                        message = "用户名或密码错误",
                        type = ErrorType.AUTH_FAILED
                    )
                    else -> throw LuciException(
                        code = statusCode,
                        message = "认证失败 (错误码: $statusCode)",
                        type = ErrorType.AUTH_FAILED
                    )
                }
            }

            val data = result[1] as? Map<*, *>
            authToken = data?.get("ubus_rpc_session")?.toString() ?: ""

            if (authToken.isEmpty()) {
                throw LuciException(message = "未获取到会话令牌", type = ErrorType.AUTH_FAILED)
            }

            return authToken
        } catch (e: Exception) {
            throw wrapException(e)
        }
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

        try {
            val request = LuciRpcRequest.callRequest(authToken, obj, method, params)
            val response = api.call(LuciApiService.UBUS_PATH, request)

            if (response.error != null) {
                // 检查是否是session过期
                if (response.error.code == -32002 || response.error.message?.contains("session", true) == true) {
                    // 尝试重新登录
                    reLogin()
                    return callUbus(obj, method, params)
                }
                throw LuciException(
                    code = response.error.code,
                    message = response.error.message
                )
            }

            val result = response.result
            if (result == null || result.size < 2) {
                throw LuciException(message = "无效的响应格式")
            }

            val statusCode = (result[0] as? Number)?.toInt() ?: -1
            if (statusCode != 0) {
                // session 过期，尝试重新登录
                if (statusCode == -32002 || statusCode == -6) {
                    reLogin()
                    return callUbus(obj, method, params)
                }
                throw LuciException(
                    code = statusCode,
                    message = "调用失败 (错误码: $statusCode)"
                )
            }

            @Suppress("UNCHECKED_CAST")
            return result[1] as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            throw wrapException(e)
        }
    }

    /**
     * 重新登录（session过期时）
     */
    private suspend fun reLogin() {
        if (currentUsername.isNotEmpty()) {
            authToken = ""
            login(currentAddress, currentUsername, currentPassword)
        }
    }

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): Map<String, Any> {
        return try {
            callUbus("system", "info")
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 获取板载信息
     */
    suspend fun getBoardInfo(): Map<String, Any> {
        return try {
            callUbus("system", "board")
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 获取路由器状态
     */
    suspend fun getRouterStatus(): RouterStatus {
        val sysInfo = getSystemInfo()
        val boardInfo = getBoardInfo()

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

    /**
     * 包装异常，提供更友好的错误信息
     */
    private fun wrapException(e: Exception): Exception {
        return when (e) {
            is LuciException -> e
            is UnknownHostException -> LuciException(
                message = "无法连接到路由器，请检查地址是否正确",
                type = ErrorType.NETWORK_ERROR
            )
            is ConnectException -> LuciException(
                message = "连接被拒绝，请检查路由器是否在线",
                type = ErrorType.NETWORK_ERROR
            )
            is SocketTimeoutException -> LuciException(
                message = "连接超时，请检查网络和路由器状态",
                type = ErrorType.TIMEOUT
            )
            is retrofit2.HttpException -> {
                when (e.code()) {
                    404 -> LuciException(
                        message = "API接口不存在 (404)，请确认路由器已安装LuCI",
                        type = ErrorType.NOT_FOUND
                    )
                    403 -> LuciException(
                        message = "访问被拒绝 (403)",
                        type = ErrorType.FORBIDDEN
                    )
                    401 -> LuciException(
                        message = "认证失败 (401)",
                        type = ErrorType.AUTH_FAILED
                    )
                    else -> LuciException(
                        message = "HTTP错误: ${e.code()}",
                        type = ErrorType.HTTP_ERROR
                    )
                }
            }
            else -> LuciException(
                message = e.message ?: "未知错误",
                type = ErrorType.UNKNOWN
            )
        }
    }

    fun getCurrentAddress(): String = currentAddress
    fun isLoggedIn(): Boolean = authToken.isNotEmpty()
    fun logout() {
        authToken = ""
        currentAddress = ""
        currentUsername = ""
        currentPassword = ""
        RetrofitClient.reset()
    }
}

/**
 * LuCI 异常类
 */
class LuciException(
    val code: Int = -1,
    override val message: String,
    val type: ErrorType = ErrorType.UNKNOWN
) : Exception(message)

/**
 * 错误类型枚举
 */
enum class ErrorType {
    NETWORK_ERROR,    // 网络错误
    TIMEOUT,          // 超时
    AUTH_FAILED,      // 认证失败
    NOT_FOUND,        // 404
    FORBIDDEN,        // 403
    HTTP_ERROR,       // 其他HTTP错误
    UNKNOWN           // 未知错误
}
