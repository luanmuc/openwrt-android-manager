package org.openwrt.manager

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import org.openwrt.manager.data.repository.RouterRepository
import kotlin.system.exitProcess

/**
 * Application 类
 */
class OpenWrtApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化全局异常处理
        initGlobalExceptionHandler()
        // 初始化仓库
        RouterRepository.getInstance(this)
    }

    /**
     * 初始化全局异常处理器
     * 确保APP不会因为未捕获的异常而闪退
     */
    private fun initGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("OpenWrtApp", "未捕获的异常: ${throwable.message}", throwable)
                // 这里可以添加崩溃日志上报逻辑
                // 暂时只记录日志，不让APP崩溃
                // 注意：某些严重异常可能无法恢复，这里尽量尝试恢复
            } catch (e: Exception) {
                Log.e("OpenWrtApp", "异常处理失败", e)
            }

            // 尝试让默认处理器处理（可能会导致APP退出，但至少有日志）
            // 为了用户体验，我们先尝试恢复，如果不行再退出
            try {
                defaultHandler?.uncaughtException(thread, throwable)
            } catch (e: Exception) {
                // 如果默认处理器也失败，就自己处理
                Log.e("OpenWrtApp", "默认异常处理器失败", e)
            }
        }
    }

    companion object {
        /**
         * 获取Application实例
         */
        fun get(context: Context): OpenWrtApp {
            return context.applicationContext as OpenWrtApp
        }
    }
}
