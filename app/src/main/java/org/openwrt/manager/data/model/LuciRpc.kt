package org.openwrt.manager.data.model

/**
 * LuCI ubus RPC 请求封装
 * 基于 OpenWrt rpcd ubus JSON-RPC 2.0 协议
 */
data class LuciRpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String = "call",
    val params: List<Any>
) {
    companion object {
        /**
         * 创建登录请求
         */
        fun loginRequest(username: String, password: String): LuciRpcRequest {
            return LuciRpcRequest(
                id = 1,
                params = listOf(
                    "00000000000000000000000000000000",
                    "session",
                    "login",
                    mapOf(
                        "username" to username,
                        "password" to password
                    )
                )
            )
        }

        /**
         * 创建通用调用请求
         */
        fun callRequest(
            sessionId: String,
            obj: String,
            method: String,
            params: Map<String, Any> = emptyMap()
        ): LuciRpcRequest {
            return LuciRpcRequest(
                id = (Math.random() * 10000).toInt(),
                params = listOf(sessionId, obj, method, params)
            )
        }
    }
}

/**
 * LuCI ubus RPC 响应封装
 * result 是一个数组：[statusCode, data]
 * statusCode 0 表示成功
 */
data class LuciRpcResponse<T>(
    val jsonrpc: String?,
    val id: Int,
    val result: T?,
    val error: LuciRpcError?
)

data class LuciRpcError(
    val code: Int,
    val message: String
)

/**
 * 登录响应数据
 */
data class LoginResult(
    val ubus_rpc_session: String,
    val expires: Int?,
    val timeout: Int?,
    val username: String?,
    val section: String?
)

/**
 * 系统信息响应
 */
data class SystemInfo(
    val hostname: String?,
    val model: String?,
    val release: String?,
    val kernel: String?,
    val uptime: Long?,
    val load: List<Float>?,
    val memory: MemoryInfo?
)

data class MemoryInfo(
    val total: Long?,
    val free: Long?,
    val cached: Long?,
    val buffered: Long?,
    val shared: Long?
)
