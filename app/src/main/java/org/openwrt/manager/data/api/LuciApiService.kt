package org.openwrt.manager.data.api

import org.openwrt.manager.data.model.LuciRpcRequest
import org.openwrt.manager.data.model.LuciRpcResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * LuCI RPC API 接口
 * 基于 OpenWrt LuCI ubus RPC 协议
 */
interface LuciApiService {

    /**
     * 通用 RPC 调用
     */
    @POST
    suspend fun call(
        @Url url: String,
        @Body request: LuciRpcRequest
    ): LuciRpcResponse<Any>

    companion object {
        const val RPC_PATH = "/cgi-bin/luci/rpc"
        const val AUTH_PATH = "/cgi-bin/luci/rpc/auth"
        const val SYS_PATH = "/cgi-bin/luci/rpc/sys"
        const val UCI_PATH = "/cgi-bin/luci/rpc/uci"
        const val FS_PATH = "/cgi-bin/luci/rpc/fs"
        const val IPKG_PATH = "/cgi-bin/luci/rpc/ipkg"
    }
}
