package com.luanmuc.openwrtmanager.data.api

import com.luanmuc.openwrtmanager.data.model.LuciRpcRequest
import com.luanmuc.openwrtmanager.data.model.LuciRpcResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * LuCI ubus RPC API 接口
 * 基于 OpenWrt LuCI ubus JSON-RPC 2.0 协议
 * 适用于 ImmortalWrt / OpenWrt 21.02+ 新版 LuCI (JS版)
 */
interface LuciApiService {
    /**
     * 通用 ubus RPC 调用
     */
    @POST
    suspend fun call(
        @Url url: String,
        @Body request: LuciRpcRequest
    ): LuciRpcResponse<List<Any>>

    companion object {
        // ubus RPC 端点（新版 LuCI / ImmortalWrt）
        const val UBUS_PATH = "/cgi-bin/luci/admin/ubus"

        // 旧版 LuCI RPC 端点（兼容 Lua 版 LuCI）
        const val RPC_AUTH_PATH = "/cgi-bin/luci/rpc/auth"
        const val RPC_SYS_PATH = "/cgi-bin/luci/rpc/sys"
    }
}
