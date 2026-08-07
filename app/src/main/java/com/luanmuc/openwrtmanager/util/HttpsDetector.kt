package com.luanmuc.openwrtmanager.util

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 自动HTTPS检测工具
 * 自动检测路由器使用HTTP还是HTTPS
 */
object HttpsDetector {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    /**
     * 检测最佳协议
     * @param address 路由器地址（不带协议）
     * @return 最佳协议（https 或 http）
     */
    suspend fun detectBestProtocol(address: String): String {
        // 先尝试HTTPS
        if (tryHttps(address)) {
            return "https"
        }
        // HTTPS失败，尝试HTTP
        if (tryHttp(address)) {
            return "http"
        }
        // 都失败，默认返回https
        return "https"
    }

    private fun tryHttps(address: String): Boolean {
        return try {
            val url = "https://$address/cgi-bin/luci"
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            val response = client.newCall(request).execute()
            response.close()
            // 只要能连接上（不管状态码），就说明HTTPS可用
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun tryHttp(address: String): Boolean {
        return try {
            val url = "http://$address/cgi-bin/luci"
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            val response = client.newCall(request).execute()
            response.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
