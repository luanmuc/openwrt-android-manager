package com.luanmuc.openwrtmanager.util

import kotlinx.coroutines.delay
import java.io.IOException

/**
 * 重试工具类
 */
object RetryUtil {
    /**
     * 带指数退避的重试
     * @param maxRetries 最大重试次数
     * @param initialDelayMs 初始延迟（毫秒）
     * @param maxDelayMs 最大延迟（毫秒）
     * @param factor 退避因子
     * @param block 要执行的代码块
     */
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 8000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        for (retry in 0..maxRetries) {
            try {
                return block()
            } catch (e: IOException) {
                lastException = e
                if (retry < maxRetries) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
                }
            } catch (e: Exception) {
                // 非网络异常不重试
                throw e
            }
        }

        throw lastException ?: RuntimeException("Unknown error")
    }

    /**
     * 判断是否是网络错误
     */
    fun isNetworkError(e: Exception): Boolean {
        return e is IOException ||
                e is java.net.SocketTimeoutException ||
                e is java.net.ConnectException ||
                e is java.net.UnknownHostException
    }
}
