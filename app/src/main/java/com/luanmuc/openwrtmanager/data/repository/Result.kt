package com.luanmuc.openwrtmanager.data.repository

/**
 * 统一结果封装
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String = "") : Result<Nothing>()
    object Loading : Result<Nothing>()

    fun getOrNull(): T? = (this as? Success)?.data
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is Loading")
    }
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
}

/**
 * 错误类型枚举
 */
enum class ErrorType {
    NETWORK_ERROR,
    SESSION_EXPIRED,
    AUTH_FAILED,
    PERMISSION_DENIED,
    SERVER_ERROR,
    UNKNOWN_ERROR
}

/**
 * Luci异常类
 */
class LuciException(
    val errorType: ErrorType,
    message: String = "",
    cause: Throwable? = null
) : Exception(message, cause)
