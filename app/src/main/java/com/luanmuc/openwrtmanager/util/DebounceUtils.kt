package com.luanmuc.openwrtmanager.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 防抖和节流工具类
 * 用于处理快速重复的点击事件和输入事件
 */
object DebounceUtils {
    
    /**
     * 防抖：在指定时间内多次调用，只执行最后一次
     * 适用于搜索输入、按钮点击等场景
     */
    class Debouncer(
        private val delayMillis: Long = 300L,
        private val scope: CoroutineScope
    ) {
        private var job: Job? = null
        
        /**
         * 执行防抖操作
         */
        fun execute(action: () -> Unit) {
            job?.cancel()
            job = scope.launch {
                delay(delayMillis)
                action()
            }
        }
        
        /**
         * 立即执行，取消等待中的操作
         */
        fun flush(action: () -> Unit) {
            job?.cancel()
            action()
        }
        
        /**
         * 取消等待中的操作
         */
        fun cancel() {
            job?.cancel()
            job = null
        }
    }
    
    /**
     * 节流：在指定时间内只执行第一次
     * 适用于按钮点击、网络请求等场景
     */
    class Throttler(
        private val intervalMillis: Long = 500L
    ) {
        private var lastExecutionTime: Long = 0L
        
        /**
         * 执行节流操作
         * @return true表示执行了操作，false表示被节流
         */
        fun execute(action: () -> Unit): Boolean {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastExecutionTime >= intervalMillis) {
                lastExecutionTime = currentTime
                action()
                return true
            }
            return false
        }
        
        /**
         * 重置节流计时器
         */
        fun reset() {
            lastExecutionTime = 0L
        }
    }
    
    /**
     * 简单的防抖函数
     * 使用方法：val debounced = debounce(300L) { doSomething() }
     * 然后调用 debounced()
     */
    fun debounce(
        delayMillis: Long = 300L,
        scope: CoroutineScope,
        action: () -> Unit
    ): () -> Unit {
        val debouncer = Debouncer(delayMillis, scope)
        return { debouncer.execute(action) }
    }
    
    /**
     * 简单的节流函数
     * 使用方法：val throttled = throttle(500L) { doSomething() }
     * 然后调用 throttled()
     */
    fun throttle(
        intervalMillis: Long = 500L,
        action: () -> Unit
    ): () -> Boolean {
        val throttler = Throttler(intervalMillis)
        return { throttler.execute(action) }
    }
    
    /**
     * 点击防抖：防止快速重复点击
     * 默认500ms内只响应第一次点击
     */
    private val clickThrottler = Throttler(500L)
    
    /**
     * 检查是否可以执行点击（防抖）
     * @return true表示可以执行，false表示被防抖拦截
     */
    fun canClick(): Boolean {
        var executed = false
        clickThrottler.execute { executed = true }
        return executed
    }
    
    /**
     * 执行带防抖的点击操作
     * @return true表示执行了操作，false表示被防抖拦截
     */
    fun onClick(action: () -> Unit): Boolean {
        return clickThrottler.execute(action)
    }
    
    /**
     * 重置点击防抖计时器
     */
    fun resetClickDebounce() {
        clickThrottler.reset()
    }
}