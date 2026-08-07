package org.openwrt.manager.data.model

/**
 * LuCI RPC 请求封装
 */
data class LuciRpcRequest(
    val id: Int,
    val method: String,
    val params: List<Any>
)

/**
 * LuCI RPC 响应封装
 */
data class LuciRpcResponse<T>(
    val id: Int,
    val result: T?,
    val error: LuciRpcError?
)

data class LuciRpcError(
    val code: Int,
    val message: String
)
